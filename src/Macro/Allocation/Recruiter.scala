package Macro.Allocation

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val unitsByLock     : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val unassignedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val activeLocks     : mutable.Set[LockUnits]                                    = mutable.Set.empty

  def update() {
    // Remove ineligible units
    unitsByLock.values.foreach(_.filterNot(eligible).foreach(unassign))
    
    // Free units held by inactive locks
    unitsByLock.keys.filterNot(activeLocks.contains).foreach(release)
    activeLocks.clear()

    // Populate unassigned units
    unassignedUnits.clear()
    With.units.ours
      .filter(unit => eligible(unit) && ! unitsByLock.values.exists(_.contains(unit)))
      .foreach(unassignedUnits.add)
  }

  def eligible(unit: FriendlyUnitInfo): Boolean = unit.aliveAndComplete && unit.unitClass.orderable

  def onUnitDestroyed(unit: FriendlyUnitInfo) {
    unassign(unit)
    unassignedUnits.remove(unit)
  }

  def add(lock: LockUnits) {
    activeLocks.add(lock)
    unitsByLock(lock) = unitsByLock.getOrElse(lock, mutable.Set.empty)
    tryToSatisfy(lock)
  }

  def inquire(lock: LockUnits, isDryRun: Boolean): Option[Iterable[FriendlyUnitInfo]] = {

    // Offer batches of unit for the lock to choose.
    //  Batch 0: Current units
    //  Batch 1: Unassigned units
    //  Batch 2+: Units assigned to weaker-priority locks
    //
    val assignedToLowerPriority = unitsByLock.keys
      .view
      .filter(otherRequest =>
        (otherRequest.interruptable.get || lock.canPoach.get)
        && lock.owner.priority < otherRequest.owner.priority)
      .flatMap(getUnits)

    lock.offerUnits(
      unitsByLock.getOrElse(lock, Iterable.empty).view
        ++ unassignedUnits
        ++ assignedToLowerPriority,
      isDryRun)
  }

  private def tryToSatisfy(lock: LockUnits) {

    val requiredUnits = inquire(lock, isDryRun = false)

    if (requiredUnits.isEmpty) {
      release(lock)
    } else {
      // 1. Unassign all the current units
      // 2. Unassign all the required units
      // 3. Assign all the required unit
      val unitsBefore   = getUnits(lock)
      val unitsAfter    = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew      = unitsAfter.diff(unitsBefore)

      unitsObsolete.foreach(unassign)
      unitsNew.foreach(unassign)
      unitsNew.foreach(assign(_, lock))
    }
  }

  def release(lock: LockUnits) {
    unitsByLock.get(lock).foreach(_.foreach(unassign))
    unitsByLock.remove(lock)
  }

  def release(plan: Prioritized): Unit = {
    unitsByLock.keys.foreach(lock => if (lock.owner == plan) release(lock))
  }
  
  private def assign(unit: FriendlyUnitInfo, lock: LockUnits) {
    unitsByLock(lock).add(unit)
    unassignedUnits.remove(unit)
  }
  
  private def unassign(unit: FriendlyUnitInfo) {
    unassignedUnits.add(unit)
    unitsByLock.find(_._2.contains(unit)).foreach(pair => unitsByLock(pair._1).remove(unit))
  }
  
  def getUnits(lock: LockUnits): collection.Set[FriendlyUnitInfo] = {
    unitsByLock.getOrElse(lock, Set.empty)
  }
  
  def audit: Vector[(Prioritized, mutable.Set[FriendlyUnitInfo])] = {
    unitsByLock.toVector.sortBy(_._1.owner.priority).map(p => (p._1.owner, p._2))
  }
}
