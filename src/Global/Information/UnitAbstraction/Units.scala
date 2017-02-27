package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import bwapi.Position
import scala.collection.JavaConverters._

class Units {
  
  val _friendlyUnitTracker = new FriendlyUnitTracker
  val _foreignUnitTracker = new ForeignUnitTracker
  
  def get(id:Int):Option[UnitInfo] = {
    _friendlyUnitTracker.get(id).orElse(_foreignUnitTracker.get(id))
  }
  
  def get(unit:bwapi.Unit):Option[UnitInfo] = {
    if (unit == null) return None
    get(unit.getID)
  }
  
  def all:Iterable[UnitInfo] = {
    _friendlyUnitTracker.units ++ _foreignUnitTracker.units
  }
  
  def ours:Iterable[FriendlyUnitInfo] = {
    _friendlyUnitTracker.units.filter(_.player == With.game.self)
  }
  
  def enemy:Iterable[ForeignUnitInfo] = {
    _foreignUnitTracker.units
  }
  
  def ally:Iterable[FriendlyUnitInfo] = {
    _friendlyUnitTracker.units.filter(_.player != With.game.self)
  }
  
  def _remap(units:java.util.List[bwapi.Unit]):Iterable[UnitInfo] = {
    units.asScala.map(get).filter(_.nonEmpty).map(_.get)
  }
  
  def inRadius(position:Position, range:Int):Iterable[UnitInfo] = {
    _remap(With.game.getUnitsInRadius(position, range))
  }
  
  def inRectangle(topLeft:Position, bottomRight:Position):Iterable[UnitInfo] = {
    _remap(With.game.getUnitsInRectangle(topLeft, bottomRight))
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
