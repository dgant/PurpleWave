package ProxyBwapi.UnitTracking

import Lifecycle.With
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
    //
    // Note that this only gets our own units and totally ignores allied units!
    
    val friendlyUnitsByIdNew = With.self.rawUnits.map(unit => (unit.getID, unit)).toMap
    
    val unitsToAdd = friendlyUnitsByIdNew.filterNot(pair => friendlyUnitsById.contains(pair._1))
    unitsToAdd.foreach(pair => add(pair._2))
  
    val unitsToBury = friendlyUnitsById.filterNot(pair => friendlyUnitsByIdNew.contains(pair._1))
    unitsToBury.foreach(pair => remove(pair._2))
    
    friendlyUnitsById.foreach(pair => pair._2.update(friendlyUnitsByIdNew(pair._1)))
    friendlyUnits = friendlyUnitsById.values.toSet
    ourUnits = friendlyUnits.filter(_.player == With.self)
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def add(unit: bwapi.Unit) {
    val unitInfo = new FriendlyUnitInfo(unit)
    friendlyUnitsById.put(unitInfo.id, unitInfo)
  }
  
  private def remove(id: Int) {
    friendlyUnitsById.remove(id)
  }
  
  private def remove(unit: bwapi.Unit) {
    remove(unit.getID)
  }
  
  private def remove(unit: FriendlyUnitInfo) {
    remove(unit.id)
  }
}
