package Macro.Allocation

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val unitsByLock   : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val unlockedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val activeLocks   : mutable.Set[LockUnits]                                    = mutable.Set.empty

  def isUnlocked(unit: FriendlyUnitInfo): Boolean = unlockedUnits.contains(unit)
  def unlocked: Iterable[FriendlyUnitInfo] = unlockedUnits
  def lockedBy(lock: LockUnits): collection.Set[FriendlyUnitInfo] = unitsByLock.getOrElse(lock, Set.empty)

  def lockTo(lock: LockUnits, unit: FriendlyUnitInfo): Unit = lockTo(lock, Iterable(unit))
  def lockTo(lock: LockUnits, units: Iterable[FriendlyUnitInfo]): Unit = {
    activateLock(lock)
    if ( ! units.forall(unlockedUnits.contains)) {
      With.logger.warn(f"Directly locking already-locked units to ${lock.owner}: ${units.filterNot(unlockedUnits.contains)}")
      unitsByLock.foreach(_._2 --= units)
    }
    unlockedUnits --= units
    unitsByLock(lock) ++= units
  }

  def release(lock: LockUnits) {
    unitsByLock.get(lock).foreach(_.foreach(unassign))
    unitsByLock.remove(lock)
  }

  def release(plan: Prioritized): Unit = {
    unitsByLock.keys.view.filter(_.owner == plan).foreach(release)
  }

  def update() {
    // Remove ineligible units
    unitsByLock.values.foreach(_.view.filterNot(recruitable).foreach(unassign))

    // Remove inactive locks (freeing their units)
    unitsByLock.keys.view.filterNot(activeLocks.contains).foreach(release)
    activeLocks.clear()

    // Populate unassigned units
    unlockedUnits.clear()
    unlockedUnits ++= With.units.ours
      .filter(recruitable)
      .filterNot(unit => unitsByLock.values.exists(_.contains(unit)))
  }

  // Called by LockUnits
  def inquire(lock: LockUnits, isDryRun: Boolean): Option[Iterable[FriendlyUnitInfo]] = {
    val assignedToWeakerLock = unitsByLock.keys.view
      .filter(otherLock => otherLock.interruptable && lock.owner.priority < otherLock.owner.priority)
      .flatMap(lockedBy)
    lock.offerUnits(lock.units.view ++ unlockedUnits ++ assignedToWeakerLock, isDryRun)
  }

  def satisfy(lock: LockUnits) {
    activateLock(lock)

    val requiredUnits = inquire(lock, isDryRun = false)

    if (requiredUnits.isEmpty) {
      release(lock)
    } else {
      // 1. Unassign all the current units
      // 2. Unassign all the required units
      // 3. Assign all the required unit
      val unitsBefore   = lockedBy(lock)
      val unitsAfter    = requiredUnits.get.toSet
      val unitsObsolete = unitsBefore.diff(unitsAfter)
      val unitsNew      = unitsAfter.diff(unitsBefore)

      unitsObsolete.foreach(unassign)
      unitsNew.foreach(unassign)
      unitsNew.foreach(assign(_, lock))
    }
  }

  private def activateLock(lock: LockUnits): Unit = {
    lock.owner.prioritize()
    activeLocks.add(lock)
    unitsByLock(lock) = unitsByLock.getOrElse(lock, mutable.Set.empty)
  }

  private def recruitable(unit: FriendlyUnitInfo): Boolean = unit.alive && unit.unitClass.orderable && unit.remainingCompletionFrames < With.latency.framesRemaining
  
  private def assign(unit: FriendlyUnitInfo, lock: LockUnits) {
    unitsByLock(lock).add(unit)
    unlockedUnits.remove(unit)
  }
  
  private def unassign(unit: FriendlyUnitInfo) {
    unlockedUnits.add(unit)
    unitsByLock.find(_._2.contains(unit)).foreach(pair => unitsByLock(pair._1).remove(unit))
  }

  // Debugging information
  def audit: Vector[(Prioritized, mutable.Set[FriendlyUnitInfo])] = {
    unitsByLock.toVector.sortBy(_._1.owner.priority).map(p => (p._1.owner, p._2))
  }
}
