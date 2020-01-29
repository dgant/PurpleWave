package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.immutable.HashSet
import scala.collection.mutable

class FriendlyUnitTracker {
    
  private val unitInfosById = new mutable.HashMap[Int, FriendlyUnitInfo].empty
  private var friendlyUnits: Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  var ourUnits: Set[FriendlyUnitInfo] = new HashSet[FriendlyUnitInfo]
  
  def get(id: Int): Option[FriendlyUnitInfo] = unitInfosById.get(id)
  
  def update() {
  
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
    //
    // Note that this only gets our own units and totally ignores allied units!
    
    val newBwapiUnitsById = With.self.rawUnits.map(unit => (unit.getID, unit)).toMap
  
    newBwapiUnitsById.foreach(idToUnit =>
      if ( ! unitInfosById.contains(idToUnit._1))
        add(idToUnit._2, idToUnit._1))
  
    unitInfosById.foreach(idToUnitInfo =>
      if ( ! newBwapiUnitsById.contains(idToUnitInfo._1))
        remove(idToUnitInfo._1))
    
    unitInfosById.foreach(pair => pair._2.update(newBwapiUnitsById(pair._1)))
    friendlyUnits = unitInfosById.values.toSet
    ourUnits = friendlyUnits.filter(_.player == With.self)
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    val id = unit.getID
    get(id).foreach(friendly => if (friendly.agent.canScout) With.blackboard.lastScoutDeath = With.frame)
    remove(id)
  }
  
  private def add(unit: bwapi.Unit, id: Int) {
    val unitInfo = new FriendlyUnitInfo(unit, id)
    unitInfosById.put(id, unitInfo)
  }
  
  private def remove(id: Int) {
    unitInfosById.remove(id)
  }
  
  private def remove(unit: FriendlyUnitInfo) {
    remove(unit.id)
  }
}
