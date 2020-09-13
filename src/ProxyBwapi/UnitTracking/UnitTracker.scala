package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.{Cache, TotalUnitCounter}
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ListBuffer

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
  
  def inTileRadius(tile: Tile, tiles: Int): Vector[UnitInfo] = {
    inTiles(
      Circle
        .points(tiles)
        .view
        .map(tile.add)
        .filter(_.valid))
  }
  
  def inPixelRadius(pixel: Pixel, pixels: Int): Vector[UnitInfo] = {
    val tile = pixel.tileIncluding
    val pixelsSquared = pixels * pixels
    inTiles(
      Circle
        .points(pixels / 32 + 1)
        .view
        .map(tile.add)
        .filter(_.valid))
      .filter(_.pixelCenter.pixelDistanceSquared(pixel) <= pixelsSquared)
  }
  
  def inTileRectangle(rectangle: TileRectangle): Vector[UnitInfo] = {
    inTiles(rectangle.tiles)
  }
  
  private def inTiles(tiles: Seq[Tile]): Vector[UnitInfo] = {
    // This implementation induces a copy in toVector
    // Would this be faster if it accumulated immutably?
    val output = new ListBuffer[UnitInfo]
    for (tile <- tiles) {
      if (tile.valid) {
        if (With.grids.units.rawValues(tile.i).nonEmpty) {
          output ++= With.grids.units.rawValues(tile.i)
        }
      }
    }
    tiles.view.flatMap(With.grids.units.get)
    output.toVector
  }
  
  def onFrame() {
    friendlyUnitTracker.update()
    foreignUnitTracker.update()
    all.foreach(historicalUnitTracker.remove)
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    get(unit).foreach(unitInfo => {
      historicalUnitTracker.add(unitInfo)
      if (unitInfo.isEnemy) {
        With.blackboard.enemyUnitDied = true
      }
    })
    friendlyUnitTracker.onUnitDestroy(unit)
    foreignUnitTracker.onUnitDestroy(unit)
  }
}
