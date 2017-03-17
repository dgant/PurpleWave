package ProxyBwapi.UnitTracking

import Geometry.Shapes.Circle
import Geometry.TileRectangle
import Startup.With
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.{Position, UnitType}

import scala.collection.JavaConverters._

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  
  val invalidUnitTypes = Set(
    UnitType.Protoss_Scarab,
    UnitType.Protoss_Interceptor,
    UnitType.None,
    UnitType.Unknown
  )
  
  def get(id:Int):Option[UnitInfo]              = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  def getUnit(unit:bwapi.Unit):Option[UnitInfo] = if (unit == null) None else get(unit.getID)
  def all:Set[UnitInfo]                         = ours ++ enemy ++ neutral
  def buildings:Set[UnitInfo]                   = all.filter(_.unitClass.isBuilding)
  def ours:Set[FriendlyUnitInfo]                = friendlyUnitTracker.ourUnits
  def enemy:Set[ForeignUnitInfo]                = foreignUnitTracker.enemyUnits
  def neutral:Set[ForeignUnitInfo]              = foreignUnitTracker.neutralUnits
  
  private def remap(units:java.util.List[bwapi.Unit]):Iterable[UnitInfo] = {
    units.asScala.flatMap(getUnit).toList
  }
  
  def inRadius(position:Position, range:Int):Iterable[UnitInfo] = {
    val tileRadius = range / 32 + 1
    val tile = position.tileIncluding
    Circle
      .points(tileRadius)
      .map(tile.add)
      .flatten(With.grids.units.get)
      .filter(_.pixelCenter.pixelDistanceSquared(position) <= range * range)
  }
  
  def inRectangle(topLeftInclusive:Position, bottomRightExclusive:Position):Iterable[UnitInfo] = {
    new TileRectangle(topLeftInclusive.tileIncluding, bottomRightExclusive.tileIncluding)
        .tiles
        .flatten(With.grids.units.get)
        .filter(unit =>
          unit.pixelCenter.getX >= topLeftInclusive.getX &&
          unit.pixelCenter.getY >= topLeftInclusive.getY &&
          unit.pixelCenter.getX < bottomRightExclusive.getX &&
          unit.pixelCenter.getY < bottomRightExclusive.getY)
  }
  
  def inRectangle(rectangle:TileRectangle):Iterable[UnitInfo] = {
    rectangle
      .tiles
        .flatten(With.grids.units.get)
        .filter(unit => rectangle.contains(unit.tileCenter))
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
