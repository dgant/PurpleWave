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
  
  def ours:Set[FriendlyUnitInfo] = {
    _friendlyUnitTracker.ourUnits
  }
  
  def enemy:Set[ForeignUnitInfo] = {
    _foreignUnitTracker.enemyUnits
  }
  
  def neutral:Set[ForeignUnitInfo] = {
    _foreignUnitTracker.neutralUnits
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
