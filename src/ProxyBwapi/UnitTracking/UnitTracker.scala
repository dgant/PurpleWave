package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import Performance.UnitCounter
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  
  def getId(id: Int): Option[UnitInfo] = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  
  def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else getId(unit.getID)
  
  def all: Seq[UnitInfo] = ours.toVector ++ enemy ++ neutral
  
  private val counterOurs = new UnitCounter(() => ours)
  def existsOurs(matcher: UnitMatcher*): Boolean = countOurs(matcher: _*) > 0
  def countOurs(matcher: UnitMatcher*): Int = counterOurs(matcher: _*)
  def countOurs(predicate: (FriendlyUnitInfo) => Boolean): Int = counterOurs(predicate)
  def ours: Set[FriendlyUnitInfo] = friendlyUnitTracker.ourUnits
  
  private val counterEnemy = new UnitCounter(() => enemy)
  def existsEnemy(matcher: UnitMatcher*): Boolean = countEnemy(matcher: _*) > 0
  def countEnemy(matcher: UnitMatcher*): Int = counterEnemy(matcher: _*)
  def countEnemy(predicate: (ForeignUnitInfo) => Boolean): Int = counterEnemy(predicate)
  def enemy: Set[ForeignUnitInfo] = foreignUnitTracker.enemyUnits
  
  def neutral: Set[ForeignUnitInfo] = foreignUnitTracker.neutralUnits
  
  def inTileRadius(tile: Tile, tiles: Int): Traversable[UnitInfo] = {
    inTiles(Circle.points(tiles).map(tile.add).filter(_.valid))
  }
  
  def inPixelRadius(pixel: Pixel, pixels: Int): Traversable[UnitInfo] = {
    val tile = pixel.tileIncluding
    val pixelsSquared = pixels * pixels
    inTiles(Circle.points(pixels / 32 + 1).map(tile.add).filter(_.valid))
      .filter(_.pixelCenter.pixelDistanceSquared(pixel) <= pixelsSquared)
  }
  
  def inRectangle(rectangle: TileRectangle): Traversable[UnitInfo] = {
    inTiles(rectangle.tiles).toSet
  }
  
  private def inTiles(tiles: Traversable[Tile]): Traversable[UnitInfo] = {
    tiles.flatten(tile => With.grids.units.get(tile))
  }
  
  def update() {
    friendlyUnitTracker.update()
    foreignUnitTracker.update()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    get(unit).foreach(unitInfo => if (unitInfo.isEnemy) {
      With.blackboard.enemyUnitDied = true
    })
    friendlyUnitTracker.onUnitDestroy(unit)
    foreignUnitTracker.onUnitDestroy(unit)
    
  }
}
