package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.{Cache, TotalUnitCounter}
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  val historicalUnitTracker = new HistoricalUnitTracker
  
  @inline def getId(id: Int): Option[UnitInfo] = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  @inline def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else getId(unit.getID)

  @inline def playerOwned = playedOwnedCache()
  private val playedOwnedCache = new Cache(() => ours ++ enemy)

  @inline def all = allCache()
  private val allCache = new Cache(() => playerOwned ++ neutral)
  
  private val counterOurs = new TotalUnitCounter(() => ours)
  @inline def existsOurs(matcher: UnitMatcher*): Boolean = countOurs(matcher: _*) > 0
  @inline def countOurs(matcher: UnitMatcher*): Int = counterOurs(matcher: _*)
  @inline def countOursP(predicate: (UnitInfo) => Boolean): Int = counterOurs.p(predicate)
  @inline def ours: Iterable[FriendlyUnitInfo] = friendlyUnitTracker.ourUnits
  
  private val counterEnemy = new TotalUnitCounter(() => enemy)
  @inline def existsEnemy(matcher: UnitMatcher*): Boolean = countEnemy(matcher: _*) > 0
  @inline def countEnemy(matcher: UnitMatcher*): Int = counterEnemy(matcher: _*)
  @inline def countEnemyP(predicate: (UnitInfo) => Boolean): Int = counterEnemy.p(predicate)
  @inline def enemy: Iterable[ForeignUnitInfo] = foreignUnitTracker.enemyUnits

  private val counterEver = new TotalUnitCounter(() => ever)
  @inline def existsEver(matcher: UnitMatcher*): Boolean = countEver(matcher: _*) > 0
  @inline def countEver(matcher: UnitMatcher*): Int = counterEver(matcher: _*)
  @inline def countEverP(predicate: (UnitInfo) => Boolean): Int = counterEver.p(predicate)
  @inline def ever: Iterable[UnitInfo] = all ++ historicalUnitTracker.all
  
  @inline def neutral: Iterable[ForeignUnitInfo] = foreignUnitTracker.neutralUnits

  val selected = new Cache(() => With.units.all.filter(_.selected))
  
  def inTileRadius(tile: Tile, tiles: Int): Seq[UnitInfo] = {
    inTiles(
      Circle
        .points(tiles)
        .view
        .map(tile.add)
        .filter(_.valid))
  }
  
  def inPixelRadius(pixel: Pixel, pixels: Int): Seq[UnitInfo] = {
    val pixelsSquared = pixels * pixels
    inTileRadius(pixel.tile, pixels / 32 + 1).filter(_.pixel.pixelDistanceSquared(pixel) <= pixelsSquared)
  }
  
  def inTileRectangle(rectangle: TileRectangle): Seq[UnitInfo] = inTiles(rectangle.tiles)
  
  private def inTiles(tiles: Seq[Tile]): Seq[UnitInfo] = tiles.view.flatMap(With.grids.units.get)
  
  def onFrame() {
    friendlyUnitTracker.updateFriendly()
    foreignUnitTracker.updateForeign()
    all.foreach(historicalUnitTracker.remove)
  }

  def onUnitRenegade(unit: bwapi.Unit): Unit = {
    onUnitDestroyOrRenegade(unit)
  }

  def onUnitDestroy(unit: bwapi.Unit) {
    get(unit).foreach(unitInfo => {
      historicalUnitTracker.add(unitInfo)
      if (unitInfo.isEnemy) {
        With.blackboard.enemyUnitDied = true
      }
    })
    onUnitDestroyOrRenegade(unit)
  }

  private def onUnitDestroyOrRenegade(unit: bwapi.Unit): Unit = {
    friendlyUnitTracker.onUnitDestroyOrRenegade(unit)
    foreignUnitTracker.onUnitDestroyOrRenegade(unit)
  }
}
