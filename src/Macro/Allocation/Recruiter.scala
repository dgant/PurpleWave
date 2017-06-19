package Macro.Allocation

import Planning.Composition.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran}
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val unitsByLock     : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val unassignedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val activeLocks     : mutable.Set[LockUnits]                                    = mutable.Set.empty

  def update() {
    
    // Remove ineligible units
    unitsByLock.values.foreach(_.filterNot(isEligible).foreach(unassign))
    
    // Free units held by inactive locks
    unitsByLock.keys.filterNot(activeLocks.contains).foreach(remove)
    activeLocks.clear()
    
    // Populate unassigned units
    unassignedUnits.clear()
    With.units.ours
      .filter(unit => isEligible(unit) && ! unitsByLock.values.exists(_.contains(unit)))
      .foreach(unassignedUnits.add)
    
    //If we suspect any bugginess, enable this
    //test
  }
  
  val ineligibleClasses = Set(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine)
  private def isEligible(unit:FriendlyUnitInfo):Boolean = unit.aliveAndComplete && ! ineligibleClasses.contains(unit.unitClass)
  
  private def test() {
    //Verify no units are shared between locks
    unitsByLock.foreach(pair1 =>
      unitsByLock.foreach(pair2 =>
        if (pair1 != pair2) {
          val intersection = pair1._2.intersect(pair2._2)
          if (intersection.nonEmpty) {
            With.logger.warn(pair1._1.toString + " and " + pair2._1.toString + " share " + intersection.size + " units")
          }
        }
      ))
  }
  
  def onUnitDestroyed(unit:FriendlyUnitInfo) {
    unassign(unit)
    unassignedUnits.remove(unit)
  }
  
  def add(lock: LockUnits) {
    //This lock is already up to date. Chill.
    if (activeLocks.contains(lock)) {
      //  return
    }
  
    activeLocks.add(lock)
    unitsByLock(lock) = unitsByLock.getOrElse(lock, mutable.Set.empty)
    tryToSatisfy(lock)
  }
  
  private def tryToSatisfy(lock: LockUnits) {
    
    // Offer batches of unit for the lock to choose.
    //   Batch 0: Units not assigned
    //   Batch 1+: Units assigned to weaker-priority locks
    //
    val assignedToLowerPriority = unitsByLock.keys
      .filter(otherRequest =>
        With.prioritizer.getPriority(lock.owner) <
        With.prioritizer.getPriority(otherRequest.owner))
      .map(getUnits)
    
    val requiredUnits = lock.offerUnits(Iterable(unassignedUnits) ++ assignedToLowerPriority)

    if (requiredUnits.isEmpty) {
      remove(lock)
    }
    else {
  
      // 1. Unassign all the current unit
      // 2. Unassign all the required unit
      // 3. Assign all the required unit
      val unitsBefore = getUnits(lock)
      val unitsAfter = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew = unitsAfter.diff(unitsBefore)
      
      unitsObsolete.foreach(unassign)
      unitsNew.foreach(unassign)
      unitsNew.foreach(assign(_, lock))
    }
  }
  
  def remove(lock: LockUnits) {
    unitsByLock.get(lock).foreach(_.foreach(unassign))
    unitsByLock.remove(lock)
  }
  
  private def assign(unit:FriendlyUnitInfo, lock:LockUnits) {
    unitsByLock(lock).add(unit)
    unassignedUnits.remove(unit)
  }
  
  private def unassign(unit:FriendlyUnitInfo) {
    unassignedUnits.add(unit)
    unitsByLock.find(pair => pair._2.contains(unit)).foreach(pair => unitsByLock.remove(pair._1))
  }
  
  def getUnits(lock: LockUnits):Set[FriendlyUnitInfo] = {
    unitsByLock.get(lock).map(_.toSet).getOrElse(Set.empty)
  }
}
