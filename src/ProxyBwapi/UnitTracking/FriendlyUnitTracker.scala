package ProxyBwapi.UnitTracking

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class FriendlyUnitTracker {
    
  private val unitInfosById = new mutable.HashMap[Int, FriendlyUnitInfo].empty
  var ourUnits: Iterable[FriendlyUnitInfo] = Iterable.empty
  
  def get(id: Int): Option[FriendlyUnitInfo] = unitInfosById.get(id)
  
  def update() {
  
    //Important to remember: bwapi.Units are not persisted frame-to-frame
    //So we do all our comparisons by ID, rather than by object
    //
    // Note that this only gets our own units and totally ignores allied units!
    
    val newBwapiUnitsById = With.self.rawUnits.map(unit => (unit.getID, unit)).toMap

    var newUnitsDiscovered: Int = 0
    newBwapiUnitsById.foreach(idToUnit =>
      if ( ! unitInfosById.contains(idToUnit._1)) {
        add(idToUnit._2, idToUnit._1)
        newUnitsDiscovered += 1
      })

    // Performance optimization:
    // Count whether any units are missing.
    // If not, skip this membership test.
    // The membership test is mostly a fallback anyway since we should remove these units in onUnitDestroy.
    if (unitInfosById.size + newUnitsDiscovered > newBwapiUnitsById.size) {
      unitInfosById.foreach(idToUnitInfo =>
        if (!newBwapiUnitsById.contains(idToUnitInfo._1))
          remove(idToUnitInfo._1))
    }
    
    unitInfosById.foreach(pair => pair._2.update(newBwapiUnitsById(pair._1)))

    // TODO: This can probably just stay a view or otherwise not converted to a set for performance's sake
    ourUnits = unitInfosById.values.view.filter(_.isOurs)
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    val id = unit.getID
    // TODO: Get this out of here. "With.blackboard.lastScout.exists( ! _.alive)"
    val friendly = get(id)
    friendly.filter(_.unitClass.isWorker).filter(_.agent.lastIntent.toScoutTiles.nonEmpty).foreach(u => With.blackboard.lastScoutDeath = With.frame)
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
