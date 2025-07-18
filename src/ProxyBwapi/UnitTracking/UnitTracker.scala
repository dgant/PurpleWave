package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.{Cache, TotalUnitCounter}
import ProxyBwapi.UnitInfo._
import Tactic.Squads.{GenericFriendlyUnitGroup, GenericUnitGroup, UnitGroup}
import Utilities.?
import Utilities.UnitFilters.{IsAny, UnitFilter}

import java.util.concurrent.locks.ReentrantLock
import scala.collection.JavaConverters._

final class UnitTracker {
  val maxHypotheticals = 100
  private val bwapiIds        = 10000 + maxHypotheticals // BWAPI IDs are on [0, 10k) and don't repeat
  private val units           = Array.fill[Option[UnitInfo]](bwapiIds)(None)
  private val bufferFriendly  = new UnorderedBuffer[FriendlyUnitInfo](bwapiIds)
  private val bufferEnemy     = new UnorderedBuffer[ForeignUnitInfo](bwapiIds)
  private val bufferNeutral   = new UnorderedBuffer[ForeignUnitInfo](bwapiIds)
  private val bufferHistoric  = new UnorderedBuffer[HistoricalUnitInfo](bwapiIds)
  private val hypothetical    = new UnorderedBuffer[ForeignUnitInfo](bwapiIds)

  val mutex: ReentrantLock = new ReentrantLock()

  @inline def getId(id: Int): Option[UnitInfo] = units(id)
  @inline def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else units(unit.getID)

  private def birth(bwapiUnit: bwapi.Unit): Unit = {
    val id = bwapiUnit.getID
    if (units(id).isEmpty) {
      bufferHistoric.removeIf(_.id == id)
      val newUnit =
        if (bwapiUnit.getPlayer.getID == With.self.id)                    bufferFriendly  .add(new FriendlyUnitInfo(bwapiUnit, id))
        else if (With.enemies.exists(_.id == bwapiUnit.getPlayer.getID))  bufferEnemy     .add(new ForeignUnitInfo(bwapiUnit, id))
        else                                                              bufferNeutral   .add(new ForeignUnitInfo(bwapiUnit, id))
      units(id) = Some(newUnit)
      newUnit.readProxy()
      With.scouting.onUnitBirthTimings(newUnit)
    }
  }
  private def kill(id: Int): Unit = {
    getId(id).foreach(unit => {
      bufferHistoric.add(new HistoricalUnitInfo(unit))
      unit.friendly.foreach(bufferFriendly.remove)
      unit.foreign.filter(_.isEnemy).foreach(bufferEnemy.remove)
      unit.foreign.filter(_.isNeutral).foreach(bufferNeutral.remove)
    })
    units(id) = None
  }
  private def birthAll(): Unit = {
    With.game.getAllUnits.asScala.foreach(u => if (units(u.getID).isEmpty) { birth(u) })
  }
  def onStart(): Unit = {
    birthAll()
  }
  def onFrame(): Unit = {
    try {
      mutex.lock()
      birthAll()
      all.foreach(_.update())
      foreign.filter(_.visibility == Visibility.Dead).map(_.id).foreach(kill)
    } finally {
      mutex.unlock()
    }
  }
  def onUnitRenegade(bwapiUnit: bwapi.Unit): Unit = {
    kill(bwapiUnit.getID)
    birth(bwapiUnit)
  }
  def onUnitDestroy(bwapiUnit: bwapi.Unit): Unit = {
    get(bwapiUnit).foreach(_.asInstanceOf[BWAPICachedUnitProxy].changeVisibility(Visibility.Dead))
    kill(bwapiUnit.getID)
  }

  ////////////
  // Access //
  ////////////

  @inline def ours        : Iterable[FriendlyUnitInfo]  = _ours
  @inline def enemy       : Iterable[ForeignUnitInfo]   = _enemy
  @inline def neutral     : Iterable[ForeignUnitInfo]   = With.units.bufferNeutral.all.filterNot(GhostUnit(_))
  @inline def playerOwned : Iterable[UnitInfo]          = ours ++ enemy
  @inline def foreign     : Iterable[UnitInfo]          = enemy ++ neutral
  @inline def all         : Iterable[UnitInfo]          = playerOwned ++ neutral
  @inline def dead        : Iterable[UnitInfo]          = bufferHistoric.all
  @inline def deadOurs    : Iterable[UnitInfo]          = dead.filter(_.isOurs)
  @inline def deadEnemy   : Iterable[UnitInfo]          = dead.filter(_.isEnemy)
  @inline def ever        : Iterable[UnitInfo]          = all ++ dead
  @inline def everOurs    : Iterable[UnitInfo]          = ours ++ dead.filter(_.isOurs)
  @inline def everEnemy   : Iterable[UnitInfo]          = enemy ++ dead.filter(_.isEnemy)
  @inline def selected    : Iterable[UnitInfo]          = all.filter(_.selected)

  private def _ours   : Seq[FriendlyUnitInfo] = With.units.bufferFriendly.all.filterNot(GhostUnit(_))
  private def _enemy  : Seq[ForeignUnitInfo]  = With.units.bufferEnemy.all.filterNot(GhostUnit(_))

  def inTiles(tiles: Seq[Tile]): Seq[UnitInfo] = tiles.view.flatMap(_.units)
  def inTileRectangle(rectangle: TileRectangle): Seq[UnitInfo] = inTiles(rectangle.tiles)
  def inTileRadius(tile: Tile, tiles: Int): Seq[UnitInfo] = inTiles(
    Circle(tiles)
      .view
      .map(tile.add))
  def inPixelRadius(pixel: Pixel, pixels: Int): Seq[UnitInfo] = {
    val pixelsSquared = pixels * pixels
    inTileRadius(pixel.tile, Maff.div32(pixels)).filter(_.pixel.pixelDistanceSquared(pixel) <= pixelsSquared)
  }

  //////////////
  // Counting //
  //////////////

  private val counterOurs = new TotalUnitCounter(() => ours)
  @inline def existsOurs(matcher: UnitFilter*): Boolean = countOurs(matcher: _*) > 0
  @inline def countOurs(matcher: UnitFilter*): Int = counterOurs(IsAny(matcher: _*))
  @inline def countOursP(predicate: (UnitInfo) => Boolean): Int = counterOurs.p(predicate)

  private val counterEnemy = new TotalUnitCounter(() => enemy)
  @inline def existsEnemy(matcher: UnitFilter*): Boolean = countEnemy(matcher: _*) > 0
  @inline def countEnemy(matcher: UnitFilter*): Int = counterEnemy(IsAny(matcher: _*))
  @inline def countEnemyP(predicate: (UnitInfo) => Boolean): Int = counterEnemy.p(predicate)

  private val counterEver = new TotalUnitCounter(() => With.units.ever)
  @inline def existsEver(matcher: UnitFilter*): Boolean = countEver(matcher: _*) > 0
  @inline def countEver(matcher: UnitFilter*): Int = counterEver(IsAny(matcher: _*))
  @inline def countEverP(predicate: (UnitInfo) => Boolean): Int = counterEver.p(predicate)

  private val counterEverOurs = new TotalUnitCounter(() => With.units.everOurs)
  @inline def existsEverOurs(matcher: UnitFilter*): Boolean = counterEverOurs(IsAny(matcher: _*)) > 0
  @inline def countEverOurs(matcher: UnitFilter*): Int = counterEverOurs(IsAny(matcher: _*))
  @inline def countEverOursP(predicate: (UnitInfo) => Boolean): Int = counterEverOurs.p(predicate)

  private val counterEverEnemy = new TotalUnitCounter(() => With.units.everEnemy)
  @inline def existsEverEnemy(matcher: UnitFilter*): Boolean = counterEverEnemy(IsAny(matcher: _*)) > 0
  @inline def countEverEnemy(matcher: UnitFilter*): Int = counterEverEnemy(IsAny(matcher: _*))
  @inline def countEverEnemyP(predicate: (UnitInfo) => Boolean): Int = counterEverEnemy.p(predicate)

  ////////////
  // Groups //
  ////////////

  @inline def ourGroup    : GenericFriendlyUnitGroup  = _friendlyGroup()
  @inline def enemyGroup  : UnitGroup                 = _enemyGroup()
  @inline def groupOf(unit: UnitInfo): UnitGroup = ?(unit.isFriendly, ourGroup, enemyGroup)
  @inline def groupVs(unit: UnitInfo): UnitGroup = ?(unit.isFriendly, enemyGroup, ourGroup)
  private val _friendlyGroup  = new Cache(() => GenericFriendlyUnitGroup(_ours))
  private val _enemyGroup     = new Cache(() => GenericUnitGroup(_enemy))
}
