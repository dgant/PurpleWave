package Global.Information.UnitAbstraction

import Geometry.{Circle, TileRectangle}
import Startup.With
import Types.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.Enrichment.EnrichPosition._
import bwapi.Position

import scala.collection.JavaConverters._

class Units {
  
  val _friendlyUnitTracker = new FriendlyUnitTracker
  val _foreignUnitTracker = new ForeignUnitTracker
  
  def get(id:Int):Option[UnitInfo]          = _friendlyUnitTracker.get(id).orElse(_foreignUnitTracker.get(id))
  def get(unit:bwapi.Unit):Option[UnitInfo] = if (unit == null) None else get(unit.getID)
  def all:Set[UnitInfo]                     = ours ++ enemy ++ neutral
  def buildings:Set[UnitInfo]               = all.filter(_.utype.isBuilding)
  def ours:Set[FriendlyUnitInfo]            = _friendlyUnitTracker.ourUnits
  def enemy:Set[ForeignUnitInfo]            = _foreignUnitTracker.enemyUnits
  def neutral:Set[ForeignUnitInfo]          = _foreignUnitTracker.neutralUnits
  
  def _remap(units:java.util.List[bwapi.Unit]):Iterable[UnitInfo] =
    units.asScala.map(get).filter(_.nonEmpty).map(_.get)
  
  def inRadius(position:Position, range:Int):Iterable[UnitInfo] = {
    val tileRadius = range / 32 + 1
    val tile = position.tileIncluding
    Circle
      .points(tileRadius)
      .map(tile.add)
      .flatten(With.grids.units.get)
      .filter(_.position.pixelDistanceSquared(position) <= range)
  }
  
  def inRectangle(topLeftInclusive:Position, bottomRightExclusive:Position):Iterable[UnitInfo] = {
    new TileRectangle(topLeftInclusive.tileIncluding, bottomRightExclusive.tileIncluding)
        .tiles
        .flatten(With.grids.units.get)
        .filter(unit =>
          unit.position.getX >= topLeftInclusive.getX &&
          unit.position.getY >= topLeftInclusive.getY &&
          unit.position.getX < bottomRightExclusive.getX &&
          unit.position.getY < bottomRightExclusive.getY)
  }
  
  def inRectangle(rectangle:TileRectangle):Iterable[UnitInfo] = {
    rectangle
      .tiles
        .flatten(With.grids.units.get)
        .filter(unit => rectangle.contains(unit.tileCenter))
  }
  
  def onFrame() {
    _friendlyUnitTracker.onFrame()
    _foreignUnitTracker.onFrame()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    _friendlyUnitTracker.onUnitDestroy(unit)
    _foreignUnitTracker.onUnitDestroy(unit)
  }
}
