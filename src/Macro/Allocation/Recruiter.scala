package Macro.Allocation

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val unitsByLock   : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val unlockedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val activeLocks   : mutable.ArrayBuffer[LockUnits]                            = mutable.ArrayBuffer.empty

  def available: Iterable[FriendlyUnitInfo] = unlockedUnits.view ++ unitsByLock.view.filter(_._1.interruptable).filterNot(_._1.owner.isPrioritized).flatMap(_._2.view)
  def lockOf(unit: FriendlyUnitInfo): Option[LockUnits] = activeLocks.find(_.units.contains(unit))
  def lockedBy(lock: LockUnits): collection.Set[FriendlyUnitInfo] = unitsByLock.getOrElse(lock, Set.empty)
  def locksOf(owner: Prioritized): Iterable[LockUnits] = unitsByLock.view.filter(_._1.owner == owner).map(_._1)
  def lockedBy(owner: Prioritized): Iterable[FriendlyUnitInfo] = locksOf(owner).flatMap(_.units.view)


  def update() {
    // Remove ineligible units
    unitsByLock.values.foreach(_.view.filterNot(recruitable).foreach(unassign))

    // 1. Deactivate all locks that haven't been renewed.
    // 2. Clear active locks. If they are not renewed on this update, deactivate them on the next.
    unitsByLock.keys.view.filterNot(activeLocks.contains).foreach(deactivate)
    activeLocks.clear()

    // Populate unassigned units
    unlockedUnits.clear()
    unlockedUnits ++= With.units.ours
      .filter(recruitable)
      .filterNot(unit => unitsByLock.values.exists(_.contains(unit)))
  }

  def lockTo(lock: LockUnits, unit: FriendlyUnitInfo): Unit = lockTo(lock, Iterable(unit))
  def lockTo(lock: LockUnits, units: Iterable[FriendlyUnitInfo]): Unit = {
    activate(lock)
    unitsByLock.foreach(_._2 --= units)
    unlockedUnits --= units
    unitsByLock(lock) ++= units
  }

  def deactivate(lock: LockUnits) {
    unitsByLock.get(lock).foreach(_.foreach(unassign))
    unitsByLock.remove(lock)
    activeLocks -= lock
  }

  def renew(owner: Prioritized): Unit = {
    unitsByLock.keys.view.filter(_.owner == owner).foreach(lock => {
      unlockedUnits --= lock.units
      activate(lock)
    })
  }

  def release(owner: Prioritized): Unit = {
    unitsByLock.keys.view.filter(_.owner == owner).foreach(activate)
  }

  def inquire(lock: LockUnits, isDryRun: Boolean): Option[Iterable[FriendlyUnitInfo]] = {
    lock.offerUnits(lock.units.view ++ available, isDryRun)
  }

  def satisfy(lock: LockUnits) {
    activate(lock)
    val requiredUnits = inquire(lock, isDryRun = false)
    if (requiredUnits.isEmpty) {
      deactivate(lock)
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

  private def activate(lock: LockUnits): Unit = {
    lock.owner.prioritize()
    activeLocks.append(lock)
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

  def audit: Vector[(Prioritized, mutable.Set[FriendlyUnitInfo])] = {
    unitsByLock.toVector.sortBy(_._1.owner.priority).map(p => (p._1.owner, p._2))
  }
}
