package ProxyBwapi.UnitTracking

import Geometry.Shapes.Circle
import Geometry.TileRectangle
import Startup.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition, UnitType}

import scala.collection.JavaConverters._

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  
  val invalidUnitTypes = Set(
    UnitType.None,
    UnitType.Unknown
  )
  
  def alive(id:Int):Boolean                     = getId(id).exists(_.alive)
  def getId(id:Int):Option[UnitInfo]            = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  def get(unit:bwapi.Unit):Option[UnitInfo]     = if (unit == null) None else getId(unit.getID)
  def all:Set[UnitInfo]                         = ours ++ enemy ++ neutral
  def buildings:Set[UnitInfo]                   = all.filter(_.unitClass.isBuilding)
  def ours:Set[FriendlyUnitInfo]                = friendlyUnitTracker.ourUnits
  def enemy:Set[ForeignUnitInfo]                = foreignUnitTracker.enemyUnits
  def neutral:Set[ForeignUnitInfo]              = foreignUnitTracker.neutralUnits
  
  private def remap(units:java.util.List[bwapi.Unit]):Iterable[UnitInfo] = {
    units.asScala.flatMap(get).toList
  }
  
  def inTileRadius(tile:TilePosition, tiles:Int):Set[UnitInfo] = {
    Circle
      .points(tiles)
      .map(tile.add)
      .flatten(With.grids.units.get)
      .toSet
  }
  
  def inPixelRadius(pixel:Position, pixels:Int):Set[UnitInfo] = {
    inTileRadius(pixel.tileNearest, pixels / 32 + 1).filter(_.pixelCenter.distancePixelsSquared(pixel) <= pixels * pixels)
  }
  
  def inRectangle(topLeftInclusive:Position, bottomRightExclusive:Position):Set[UnitInfo] = {
    new TileRectangle(topLeftInclusive.tileIncluding, bottomRightExclusive.tileIncluding)
      .tiles
      .flatten(With.grids.units.get)
      .filter(unit =>
        unit.pixelCenter.getX >= topLeftInclusive.getX &&
        unit.pixelCenter.getY >= topLeftInclusive.getY &&
        unit.pixelCenter.getX < bottomRightExclusive.getX &&
        unit.pixelCenter.getY < bottomRightExclusive.getY)
      .toSet
  }
  
  def inRectangle(rectangle:TileRectangle):Set[UnitInfo] = {
    rectangle
      .tiles
      .flatten(With.grids.units.get)
      .filter(unit => rectangle.contains(unit.tileCenter))
      .toSet
  }
  
  def onFrame() {
    friendlyUnitTracker.onFrame()
    foreignUnitTracker.onFrame()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    friendlyUnitTracker.onUnitDestroy(unit)
    foreignUnitTracker.onUnitDestroy(unit)
  }
}
