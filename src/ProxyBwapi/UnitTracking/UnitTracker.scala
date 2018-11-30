package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.UnitCounter
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, HistoricalUnitInfo, UnitInfo}

import scala.collection.immutable

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  val historicalUnitTracker = new HistoricalUnitTracker
  
  def getId(id: Int): Option[UnitInfo] = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  
  def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else getId(unit.getID)

  def playerOwned: IndexedSeq[UnitInfo] = ours.toIndexedSeq ++ enemy.toIndexedSeq
  def all: IndexedSeq[UnitInfo] = playerOwned ++ neutral.toIndexedSeq
  
  private val counterOurs = new UnitCounter(() => ours)
  def existsOurs(matcher: UnitMatcher*): Boolean = countOurs(matcher: _*) > 0
  def countOurs(matcher: UnitMatcher*): Int = counterOurs(matcher: _*)
  def countOursP(predicate: (UnitInfo) => Boolean): Int = counterOurs.p(predicate)
  def ours: Set[FriendlyUnitInfo] = friendlyUnitTracker.ourUnits
  
  private val counterEnemy = new UnitCounter(() => enemy)
  def existsEnemy(matcher: UnitMatcher*): Boolean = countEnemy(matcher: _*) > 0
  def countEnemy(matcher: UnitMatcher*): Int = counterEnemy(matcher: _*)
  def countEnemyP(predicate: (UnitInfo) => Boolean): Int = counterEnemy.p(predicate)
  def enemy: Set[ForeignUnitInfo] = foreignUnitTracker.enemyUnits

  private val counterEver = new UnitCounter(() => ever)
  def existsEver(matcher: UnitMatcher*): Boolean = countEver(matcher: _*) > 0
  def countEver(matcher: UnitMatcher*): Int = counterEver(matcher: _*) + countOurs(matcher: _*) + countEnemy(matcher: _*)
  def countEverP(predicate: (UnitInfo) => Boolean): Int = counterEver.p(predicate) + countOursP(predicate) + countEnemyP(predicate)
  def ever: Iterable[HistoricalUnitInfo] = historicalUnitTracker.all
  
  def neutral: Set[ForeignUnitInfo] = foreignUnitTracker.neutralUnits
  
  def inTileRadius(tile: Tile, tiles: Int): immutable.IndexedSeq[UnitInfo] = {
    inTiles(Circle.points(tiles).map(tile.add).filter(_.valid))
  }
  
  def inPixelRadius(pixel: Pixel, pixels: Int): immutable.IndexedSeq[UnitInfo] = {
    val tile = pixel.tileIncluding
    val pixelsSquared = pixels * pixels
    inTiles(Circle.points(pixels / 32 + 1).map(tile.add).filter(_.valid))
      .filter(_.pixelCenter.pixelDistanceSquared(pixel) <= pixelsSquared)
  }
  
  def inRectangle(rectangle: TileRectangle): immutable.IndexedSeq[UnitInfo] = {
    inTiles(rectangle.tiles)
  }
  
  private def inTiles(tiles: immutable.IndexedSeq[Tile]): immutable.IndexedSeq[UnitInfo] = {
    tiles.flatten(tile => With.grids.units.get(tile))
  }
  
  def update() {
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
