package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.Players.Players
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.immutable.HashSet
import scala.collection.mutable

class FriendlyUnitTracker {
  
  private val friendlyUnitsById = new mutable.HashMap[Int, FriendlyUnitInfo].empty
  private var friendlyUnits: Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  var ourUnits: Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  
  def get(someUnit: bwapi.Unit):Option[FriendlyUnitInfo] = get(someUnit.getID)
  def get(id: Int): Option[FriendlyUnitInfo] = friendlyUnitsById.get(id)
  
  def update() {
  
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
    
    val friendlyUnitsNew                = With.self.rawUnits.filter(isValidFriendlyUnit).map(unit => (unit.getID, unit)).toMap
    val friendlyUnitsOld                = friendlyUnitsById
    val friendlyIdsNew                  = friendlyUnitsNew.keySet
    val friendlyIdsOld                  = friendlyUnitsOld.keySet
    val unitsToAdd                      = friendlyIdsNew.diff(friendlyIdsOld).map(friendlyUnitsNew)
    val unitsToUpdate                   = friendlyIdsNew.intersect(friendlyIdsOld).map(friendlyUnitsNew)
    val unitsToRemoveDueToDeath         = friendlyIdsOld.diff(friendlyIdsNew)
  
    unitsToAdd.foreach(add)
    unitsToUpdate.foreach(update)
    unitsToRemoveDueToDeath.foreach(remove)
    
    //Remove no-longer-valid units
    //We have to do this after updating because it needs the latest bwapi.Units
    friendlyUnitsById.values.map(_.base).filterNot(isValidFriendlyUnit).foreach(remove)
    
    //Could speed things up by diffing instead of recreating these
    friendlyUnits = friendlyUnitsById.values.toSet
    ourUnits = friendlyUnits.filter(_.player == With.self)
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def add(unit:bwapi.Unit) {
    val unitInfo = new FriendlyUnitInfo(unit)
    friendlyUnitsById.put(unitInfo.id, unitInfo)
  }
  
  private def update(unit:bwapi.Unit) {
    friendlyUnitsById(unit.getID).base = unit
  }
  
  private def remove(id:Int) {
    friendlyUnitsById.remove(id)
  }
  
  private def remove(unit:bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def remove(unit:FriendlyUnitInfo) {
    remove(unit.id)
  }
  
  private def isValidFriendlyUnit(unit:bwapi.Unit):Boolean ={
    if (With.units.invalidUnitTypes.contains(unit.getType)) return false
    if ( ! unit.exists) return false
    Players.get(unit.getPlayer).isFriendly
  }
}
