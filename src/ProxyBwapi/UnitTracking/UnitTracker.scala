package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import bwapi.UnitType

import scala.collection.JavaConverters._

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  
  def alive(id: Int): Boolean = getId(id).exists(_.alive)
  
  def getId(id: Int): Option[UnitInfo] = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  
  def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else getId(unit.getID)
  
  def all: Set[UnitInfo] = ours ++ enemy ++ neutral
  
  def buildings: Set[UnitInfo] = all.filter(_.unitClass.isBuilding)
  
  def ours: Set[FriendlyUnitInfo] = friendlyUnitTracker.ourUnits
  
  def enemy: Set[ForeignUnitInfo] = foreignUnitTracker.enemyUnits
  
  def neutral: Set[ForeignUnitInfo] = foreignUnitTracker.neutralUnits
  
  private def remap(units: java.util.Vector[bwapi.Unit]): Iterable[UnitInfo] = {
    units.asScala.flatMap(get).toVector
  }
  
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
    With.units.all.foreach(_.update())
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    friendlyUnitTracker.onUnitDestroy(unit)
    foreignUnitTracker.onUnitDestroy(unit)
  }
}
