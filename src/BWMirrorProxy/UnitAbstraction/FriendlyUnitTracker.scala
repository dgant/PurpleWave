package BWMirrorProxy.UnitAbstraction

import Startup.With
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.mutable

class FriendlyUnitTracker {
  
  val _friendlyUnitsById = new mutable.HashMap[Int, FriendlyUnitInfo].empty
  var _friendlyUnits:Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  var _ourUnits:Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  
  def ourUnits:Set[FriendlyUnitInfo] = _ourUnits
  def get(someUnit:bwapi.Unit):Option[FriendlyUnitInfo] = get(someUnit.getID)
  def get(id:Int):Option[FriendlyUnitInfo] = _friendlyUnitsById.get(id)
  
  def onFrame() {
  
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
    
    val friendlyUnitsNew                = With.self.getUnits.asScala.filter(_isValidFriendlyUnit).map(unit => (unit.getID, unit)).toMap
    val friendlyUnitsOld                = _friendlyUnitsById
    val friendlyIdsNew                  = friendlyUnitsNew.keySet
    val friendlyIdsOld                  = friendlyUnitsOld.keySet
    val unitsToAdd                      = friendlyIdsNew.diff(friendlyIdsOld).map(friendlyUnitsNew)
    val unitsToUpdate                   = friendlyIdsNew.intersect(friendlyIdsOld).map(friendlyUnitsNew)
    val unitsToRemoveDueToDeath         = friendlyIdsOld.diff(friendlyIdsNew)
  
    unitsToAdd.foreach(_add)
    unitsToUpdate.foreach(_update)
    unitsToRemoveDueToDeath.foreach(_remove)
    
    //Remove no-longer-valid units
    //We have to do this after updating because it needs the latest bwapi.Units
    _friendlyUnitsById.values.map(_.baseUnit).filterNot(_isValidFriendlyUnit).foreach(_remove)
    
    //Could speed things up by diffing instead of recreating these
    _friendlyUnits = _friendlyUnitsById.values.toSet
    _ourUnits = _friendlyUnits.filter(_.player == With.self)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _remove(unit.getID)
  }
  
  def _add(unit:bwapi.Unit) {
    val unitInfo = new FriendlyUnitInfo(unit)
    _friendlyUnitsById.put(unitInfo.id, unitInfo)
  }
  
  def _update(unit:bwapi.Unit) {
    _friendlyUnitsById(unit.getID).baseUnit = unit
  }
  
  def _remove(id:Int) {
    _friendlyUnitsById.remove(id)
  }
  
  def _remove(unit:bwapi.Unit) {
    _remove(unit.getID)
  }
  
  def _remove(unit:FriendlyUnitInfo) {
    _remove(unit.id)
  }
  
  def _isValidFriendlyUnit(unit:bwapi.Unit):Boolean ={
    if (With.units.invalidUnitTypes.contains(unit.getType)) return false
    if ( ! unit.exists) return false
    unit.getPlayer == With.self || unit.getPlayer.isAlly(With.self)
  }
}
