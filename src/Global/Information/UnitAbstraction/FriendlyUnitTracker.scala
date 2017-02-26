package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.FriendlyUnitInfo

import scala.collection.JavaConverters._
import scala.collection.mutable

class FriendlyUnitTracker {
  
  val _friendlyUnits = new mutable.HashMap[Int, FriendlyUnitInfo].empty
  
  def units:Iterable[FriendlyUnitInfo] = _friendlyUnits.values
  
  def onFrame() {
    val trackedUnitsLiving = With.game.self.getUnits.asScala.filter(unit => _friendlyUnits.contains(unit.getID))
    val trackedUnitIdsDead = _friendlyUnits.keySet.diff(With.game.self.getUnits.asScala.map(_.getID).toSet)
    val untrackedVisibleUnits = With.game.self.getUnits.asScala
      .filter(unit => ! _friendlyUnits.contains(unit.getID))
      .filter(unit => unit.getPlayer == With.game.self || unit.getPlayer.isAlly(With.game.self()))
    
    trackedUnitsLiving.foreach(_update)
    trackedUnitIdsDead.foreach(_remove)
    untrackedVisibleUnits.foreach(_add)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _remove(unit.getID)
  }
  
  def _add(unit:bwapi.Unit) {
    val knownUnit = new FriendlyUnitInfo(unit)
    _friendlyUnits.put(knownUnit.id, new FriendlyUnitInfo(unit))
  }
  
  def _update(unit:bwapi.Unit) {
    _friendlyUnits(unit.getID).baseUnit = unit
  }
  
  def _remove(id:Int) {
    _friendlyUnits.remove(id)
  }
}
