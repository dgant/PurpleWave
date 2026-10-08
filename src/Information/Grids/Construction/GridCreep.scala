package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With
import ProxyBwapi.Players.Players
import bwapi.{Race, UnitType}

import scala.collection.JavaConverters._

object GridCreep {
  private[Grids] val ObservationPeriod: Int = 12

  private[Grids] case class InitialBuilding(unitType: UnitType, pixelX: Int, pixelY: Int, neutral: Boolean) {
    val left: Int = pixelX / 32 - unitType.tileWidth / 2
    val top : Int = pixelY / 32 - unitType.tileHeight / 2
    def isZerg: Boolean = unitType.getRace == Race.Zerg
    def spreads: Boolean = unitType match {
      case UnitType.Zerg_Hatchery | UnitType.Zerg_Lair | UnitType.Zerg_Hive |
           UnitType.Zerg_Creep_Colony | UnitType.Zerg_Sunken_Colony | UnitType.Zerg_Spore_Colony => true
      case _ => false
    }
    def seeds: Boolean = isZerg && (spreads || unitType.requiresCreep)
  }

  // OpenBW's create_initial_unit/spread_creep_completely, reduced to the final
  // footprint: https://github.com/OpenBW/openbw/blob/master/bwgame.h
  // Growth is monotone, so with fixed terrain/occupancy its random choice of the
  // next neighboring tile does not change the completed flood-fill.
  private[Grids] def seed(
    source: InitialBuilding,
    width: Int,
    height: Int,
    creep: Array[Boolean],
    occupied: Array[Boolean],
    canHaveCreep: Int => Boolean): Unit = {

    def eachFootprintTile(todo: Int => Unit): Unit = {
      var y = Math.max(0, source.top)
      while (y < Math.min(height, source.top + source.unitType.tileHeight)) {
        var x = Math.max(0, source.left)
        while (x < Math.min(width, source.left + source.unitType.tileWidth)) {
          todo(x + y * width)
          x += 1
        }
        y += 1
      }
    }

    // Initial building footprints are seeded even though the building occupies them.
    eachFootprintTile(i => if (canHaveCreep(i)) creep(i) = true)
    if ( ! source.spreads) return

    val left   = Math.max(0, (source.pixelX - 320) / 32)
    val right  = Math.min(width - 1, (source.pixelX + 320) / 32)
    val top    = Math.max(0, (source.pixelY - 200) / 32)
    val bottom = Math.min(height - 1, (source.pixelY + 200) / 32)
    val queue = new Array[Int]((right - left + 1) * (bottom - top + 1))
    var head, tail = 0

    def grow(x: Int, y: Int): Unit = {
      if (x < left || x > right || y < top || y > bottom) return
      val i = x + y * width
      if (creep(i) || occupied(i)) return
      val dx = x * 32 + 16 - source.pixelX
      val dy = y * 32 + 16 - source.pixelY
      if (25 * dx * dx + 64 * dy * dy > 25 * 320 * 320 || ! canHaveCreep(i)) return
      creep(i) = true
      queue(tail) = i
      tail += 1
    }

    // Existing creep just outside the range can also seed an eligible edge tile.
    var y = Math.max(0, top - 1)
    while (y <= Math.min(height - 1, bottom + 1)) {
      var x = Math.max(0, left - 1)
      while (x <= Math.min(width - 1, right + 1)) {
        if (creep(x + y * width)) {
          var dy = -1
          while (dy <= 1) {
            var dx = -1
            while (dx <= 1) { grow(x + dx, y + dy); dx += 1 }
            dy += 1
          }
        }
        x += 1
      }
      y += 1
    }
    while (head < tail) {
      val i = queue(head)
      head += 1
      var dy = -1
      while (dy <= 1) {
        var dx = -1
        while (dx <= 1) { grow(i % width + dx, i / width + dy); dx += 1 }
        dy += 1
      }
    }
  }
}

class GridCreep extends AbstractGridArrayBoolean {
  import GridCreep._

  private var _hadNeutralZergBuildings = false
  private var _hadZergPlayers = false
  private var creepPossible = false
  private var nextObservationFrame = 0
  private val neutralCreep = new java.util.BitSet(length)

  def hadNeutralZergBuildings: Boolean = _hadNeutralZergBuildings
  def hadZergPlayers: Boolean = _hadZergPlayers
  def initiallyNeutral(i: Int): Boolean = valid(i) && neutralCreep.get(i)

  protected def initialPlayerRaces: Seq[Race] = Players.all.filter(_.isPlayer).map(_.raceCurrent).toVector
  protected def initialBuildings: Seq[InitialBuilding] = {
    val neutral = With.game.getStaticNeutralUnits.asScala.map(u => (u, true))
    val friendly = With.game.getAllUnits.asScala
      .filter(u => u.getPlayer == With.game.self || u.getPlayer.isAlly(With.game.self))
      .map(u => (u, false))
    (neutral ++ friendly).flatMap { case (unit, isNeutral) =>
      val unitType = unit.getInitialType
      val position = unit.getInitialPosition
      if (unitType.isBuilding && position.isValid(With.game)) {
        Some(InitialBuilding(unitType, position.getX, position.getY, isNeutral))
      } else None
    }.toVector
  }

  protected def isVisible(x: Int, y: Int): Boolean = With.game.isVisibleUnsafe(x, y)
  protected def liveCreep(x: Int, y: Int): Boolean = With.game.hasCreepUnsafe(x, y)
  protected def isBuildable(x: Int, y: Int): Boolean = With.game.isBuildable(x, y)
  protected def isWalkable(x: Int, y: Int): Boolean = With.game.isWalkable(x, y)

  override def onInitialization(): Unit = {
    val buildings = initialBuildings
    val races = initialPlayerRaces
    _hadNeutralZergBuildings = buildings.exists(b => b.neutral && b.isZerg)
    _hadZergPlayers = races.contains(Race.Zerg)
    // An unrevealed Random opponent is not evidence that the game has no Zerg.
    creepPossible = _hadNeutralZergBuildings || _hadZergPlayers || races.exists(r => r == Race.Unknown || r == Race.Random)
    if ( ! creepPossible) return

    val sources = buildings.filter(_.seeds)
    if (sources.isEmpty) return
    val occupied = new Array[Boolean](length)
    buildings.foreach(b => {
      var y = Math.max(0, b.top)
      while (y < Math.min(height, b.top + b.unitType.tileHeight)) {
        var x = Math.max(0, b.left)
        while (x < Math.min(width, b.left + b.unitType.tileWidth)) {
          occupied(x + y * width) = true
          x += 1
        }
        y += 1
      }
    })

    // Cache terrain tests only for the small areas reached by initial sources.
    val terrain = Array.fill[Byte](length)(-1)
    def canHaveCreep(i: Int): Boolean = {
      if (terrain(i) < 0) {
        val x = i % width
        val y = i / width
        var allowed = isBuildable(x, y) && (y == height - 1 || isBuildable(x, y + 1))
        var w = 0
        while (allowed && w < 16) {
          allowed = isWalkable(4 * x + w % 4, 4 * y + w / 4)
          w += 1
        }
        terrain(i) = (if (allowed) 1 else 0).toByte
      }
      terrain(i) == 1
    }

    sources.filter(_.neutral).foreach(seed(_, width, height, values, occupied, canHaveCreep))
    var i = 0
    while (i < length) { if (values(i)) neutralCreep.set(i); i += 1 }
    sources.filterNot(_.neutral).foreach(seed(_, width, height, values, occupied, canHaveCreep))
  }

  override def update(): Unit = {
    initialize()
    if ( ! creepPossible || With.frame < nextObservationFrame) return
    nextObservationFrame = With.frame + ObservationPeriod
    var x = 0
    while (x < width) {
      var y = 0
      while (y < height) {
        // Never replace remembered/predicted creep with BWAPI's false under fog.
        if (isVisible(x, y)) values(x + y * width) = liveCreep(x, y)
        y += 1
      }
      x += 1
    }
  }
}
