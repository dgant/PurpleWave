package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

final class SimulationInteractionGraph(simulation: Simulation) {
  private final val ProximityBufferPixels = 64.0
  private var maxUnitExtent = 0
  private var proximityFrames = 0

  private var units   : Array[UnitInfo] = Array.empty
  private var parent  : Array[Int]      = Array.empty
  private var size    : Array[Int]      = Array.empty
  private val indices = new mutable.HashMap[UnitInfo, Int]

  def reset(newUnits: Seq[UnitInfo]): Unit = {
    units = newUnits.toArray
    maxUnitExtent = units.map(_.simulacrum.unitClass.dimensionMax).foldLeft(0)(Math.max)
    proximityFrames = Math.max(0, With.configuration.simulationProximityFrames)
    parent = new Array[Int](units.length)
    size = Array.fill(units.length)(1)
    indices.clear()
    var i = 0
    while (i < units.length) {
      parent(i) = i
      indices(units(i)) = i
      i += 1
    }
  }

  // Damage, including late hits and friendly splash, keeps scored outcomes causally connected.
  @inline def observeDamage(attacker: UnitInfo, victim: UnitInfo): Unit = union(attacker, victim)

  def observeProximity(): Unit = {
    if (simulation.battle.simulationFrames > proximityFrames) return
    var i = 0
    while (i < units.length) {
      val simulacrum = units(i).simulacrum
      if (simulacrum.alive) {
        observeProximity(simulacrum)
      }
      i += 1
    }
  }

  def components: Vector[Seq[UnitInfo]] = {
    val output = new mutable.LinkedHashMap[Int, mutable.ArrayBuffer[UnitInfo]]
    var i = 0
    while (i < units.length) {
      val root = find(i)
      output.getOrElseUpdate(root, new mutable.ArrayBuffer[UnitInfo]) += units(i)
      i += 1
    }
    output.values.map(_.toVector).toVector
  }

  private def observeProximity(unit: Simulacrum): Unit = {
    if (unit.attacksAgainstAir <= 0 && unit.attacksAgainstGround <= 0) return
    // Search grid cells by weapon reach and footprints, never detector sight range.
    val reach = Math.max(unit.pixelRangeAir, unit.pixelRangeGround) + ProximityBufferPixels
    val radius = Math.ceil(reach + unit.unitClass.dimensionMax + maxUnitExtent).toInt
    val xMin = Maff.clamp(Maff.div32(unit.pixel.x - radius), 0, With.mapTileWidth - 1)
    val xMax = Maff.clamp(Maff.div32(unit.pixel.x + radius), 0, With.mapTileWidth - 1)
    val yMin = Maff.clamp(Maff.div32(unit.pixel.y - radius), 0, With.mapTileHeight - 1)
    val yMax = Maff.clamp(Maff.div32(unit.pixel.y + radius), 0, With.mapTileHeight - 1)
    var y = yMin
    while (y <= yMax) {
      var x = xMin
      while (x <= xMax) {
        val tileUnits = simulation.grid.tiles(x + y * With.mapTileWidth).units
        var i = 0
        while (i < tileUnits.length) {
          val other = tileUnits(i)
          if (other.alive && unit.isFriendlyCached != other.isFriendlyCached && unit.canAttack(other)
            && unit.pixelDistanceEdgeSimulation(other) <= unit.pixelRangeAgainst(other) + ProximityBufferPixels) {
            union(unit.realUnit, other.realUnit)
          }
          i += 1
        }
        x += 1
      }
      y += 1
    }
  }

  @inline private def union(unitA: UnitInfo, unitB: UnitInfo): Unit = {
    val a = indices.get(unitA)
    val b = indices.get(unitB)
    if (a.isEmpty || b.isEmpty) return
    union(a.get, b.get)
  }

  @inline private def union(a: Int, b: Int): Unit = {
    var rootA = find(a)
    var rootB = find(b)
    if (rootA == rootB) return

    if (size(rootA) < size(rootB)) {
      val swap = rootA
      rootA = rootB
      rootB = swap
    }
    parent(rootB) = rootA
    size(rootA) += size(rootB)
  }

  @inline private def find(i: Int): Int = {
    var root = i
    while (parent(root) != root) {
      root = parent(root)
    }
    var next = i
    while (parent(next) != root) {
      val current = next
      next = parent(next)
      parent(current) = root
    }
    root
  }
}
