package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.TotalUnitCounter
import Planning.UnitMatchers.Matcher
import ProxyBwapi.UnitInfo._

import scala.collection.JavaConverters._

class UnitTracker {
  private val bwapiIds        = 10000 // BWAPI IDs are on [0, 10k) and don't repeat
  private val units           = Array.fill[Option[UnitInfo]](bwapiIds)(None)
  private val bufferFriendly  = new UnorderedBuffer[FriendlyUnitInfo](bwapiIds)
  private val bufferEnemy     = new UnorderedBuffer[ForeignUnitInfo](bwapiIds)
  private val bufferNeutral   = new UnorderedBuffer[ForeignUnitInfo](bwapiIds)
  private val bufferHistoric  = new UnorderedBuffer[HistoricalUnitInfo](bwapiIds)

  @inline def getId(id: Int): Option[UnitInfo] = units(id)
  @inline def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else units(unit.getID)

  private def birth(bwapiUnit: bwapi.Unit): Unit = {
    val id = bwapiUnit.getID
    if (units(id).isEmpty) {
      bufferHistoric.removeIf(_.id == id)
      val newUnit = if (bwapiUnit.getPlayer.getID == With.self.id)                    bufferFriendly  .add(new FriendlyUnitInfo(bwapiUnit, id))
        else if (With.enemies.exists(_.id == bwapiUnit.getPlayer.getID))  bufferEnemy     .add(new ForeignUnitInfo(bwapiUnit, id))
        else                                                              bufferNeutral   .add(new ForeignUnitInfo(bwapiUnit, id))
      units(id) = Some(newUnit)
      newUnit.readProxy()
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
  def onFrame() {
    With.game.getAllUnits.asScala.foreach(u => {
      val id = u.getID
      // TODO: First remove a unit if its isFriendly/isEnemy/isNeutral status has changed
      if (units(id).isEmpty) {
        birth(u)
      }
      // TODO: With new unit proxies, update proxy info here
    })
    all.foreach(_.update())
    foreign.filter(_.visibility == Visibility.Dead).map(_.id).foreach(kill)
  }
  def onUnitRenegade(bwapiUnit: bwapi.Unit): Unit = {
    With.logger.debug(f"OnUnitRenegade #${bwapiUnit.getID}")
    kill(bwapiUnit.getID)
    birth(bwapiUnit)
  }
  def onUnitDestroy(bwapiUnit: bwapi.Unit) {
    With.logger.debug(f"OnUnitDestroy #${bwapiUnit.getID}")
    get(bwapiUnit).foreach(_.asInstanceOf[BWAPICachedUnitProxy].changeVisibility(Visibility.Dead))
    kill(bwapiUnit.getID)
  }

  ////////////
  // Access //
  ////////////

  @inline final def ours        : Iterable[FriendlyUnitInfo]  = With.units.bufferFriendly.all
  @inline final def enemy       : Iterable[ForeignUnitInfo]   = With.units.bufferEnemy.all
  @inline final def neutral     : Iterable[ForeignUnitInfo]   = With.units.bufferNeutral.all
  @inline final def playerOwned : Iterable[UnitInfo]          = ours ++ enemy
  @inline final def foreign     : Iterable[UnitInfo]          = enemy ++ neutral
  @inline final def all         : Iterable[UnitInfo]          = playerOwned ++ neutral
  @inline final def ever        : Iterable[UnitInfo]          = all ++ bufferHistoric.all
  @inline final def selected    : Iterable[UnitInfo]          = all.filter(_.selected)

  def inTiles(tiles: Seq[Tile]): Seq[UnitInfo] = tiles.view.flatMap(With.grids.units.get)
  def inTileRectangle(rectangle: TileRectangle): Seq[UnitInfo] = inTiles(rectangle.tiles)
  def inTileRadius(tile: Tile, tiles: Int): Seq[UnitInfo] = inTiles(
    Circle
      .points(tiles)
      .view
      .map(tile.add))
  def inPixelRadius(pixel: Pixel, pixels: Int): Seq[UnitInfo] = {
    val pixelsSquared = pixels * pixels
    inTileRadius(pixel.tile, pixels / 32 + 1).filter(_.pixel.pixelDistanceSquared(pixel) <= pixelsSquared)
  }

  //////////////
  // Counting //
  //////////////

  private val counterOurs = new TotalUnitCounter(() => ours)
  @inline def existsOurs(matcher: Matcher*): Boolean = countOurs(matcher: _*) > 0
  @inline def countOurs(matcher: Matcher*): Int = counterOurs(matcher: _*)
  @inline def countOursP(predicate: (UnitInfo) => Boolean): Int = counterOurs.p(predicate)

  private val counterEnemy = new TotalUnitCounter(() => enemy)
  @inline def existsEnemy(matcher: Matcher*): Boolean = countEnemy(matcher: _*) > 0
  @inline def countEnemy(matcher: Matcher*): Int = counterEnemy(matcher: _*)
  @inline def countEnemyP(predicate: (UnitInfo) => Boolean): Int = counterEnemy.p(predicate)

  private val counterEver = new TotalUnitCounter(() => With.units.ever)
  @inline def existsEver(matcher: Matcher*): Boolean = countEver(matcher: _*) > 0
  @inline def countEver(matcher: Matcher*): Int = counterEver(matcher: _*)
  @inline def countEverP(predicate: (UnitInfo) => Boolean): Int = counterEver.p(predicate)
}
