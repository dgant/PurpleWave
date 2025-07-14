package Macro.Allocation

import Lifecycle.With
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Recruiter {
  
  private val unitsByLock   : mutable.HashMap[LockUnits, mutable.Set[FriendlyUnitInfo]] = mutable.HashMap.empty
  private val unlockedUnits : mutable.Set[FriendlyUnitInfo]                             = mutable.Set.empty
  private val activeLocks   : mutable.ArrayBuffer[LockUnits]                            = mutable.ArrayBuffer.empty

  def available: Iterable[FriendlyUnitInfo] = unlockedUnits.view ++ unitsByLock.view.filter(_._1.interruptable).filterNot(_._1.owner.isPrioritized).flatMap(_._2.view)
  def locks: Seq[LockUnits] = activeLocks
  def lockedBy(lock: LockUnits): collection.Set[FriendlyUnitInfo] = unitsByLock.getOrElse(lock, Set.empty)
  def locksOf(owner: Prioritized): Iterable[LockUnits] = unitsByLock.view.filter(_._1.owner == owner).map(_._1)
  def lockedBy(owner: Prioritized): Iterable[FriendlyUnitInfo] = locksOf(owner).flatMap(_.units.view)

  def update(): Unit = {
    // Remove ineligible units
    unitsByLock.foreach { case (lock, units) => units.view
      .filterNot(recruitable)
      .filterNot(lock.matcher)
      .foreach(unlock)
    }

    // Ablate locks
    unitsByLock.foreach { case (lock, units) => {
      val excess = units.size - lock.counter.maximum
      if (excess > 0) {
        val ablate = Maff.takeN(excess, units)(Ordering.by(u => lock.preference(u))) // TakeN uses PriorityQueue, so it's high-to-low
        ablate.foreach(unlock)
      }
    }}

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

  def deactivate(lock: LockUnits): Unit = {
    unitsByLock.get(lock).foreach(_.foreach(unlock))
    unitsByLock.remove(lock)
    activeLocks -= lock
  }

  def renew(owner: Prioritized): Unit = {
    locksOf(owner).foreach(lock => {
      unlockedUnits --= lock.units
      activate(lock)
    })
  }

  def release(owner: Prioritized): Unit = {
    locksOf(owner).foreach(_.release())
  }

  def inquire(lock: LockUnits, isDryRun: Boolean): Option[Iterable[FriendlyUnitInfo]] = {
    lock.offerUnits(lock.units.view ++ available, isDryRun)
  }

  def satisfy(lock: LockUnits): Unit = {
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

      unitsObsolete.foreach(unlock)
      unitsNew.foreach(unlock)
      unitsNew.foreach(lockTo(_, lock))
    }
  }

  private def activate(lock: LockUnits): Unit = {
    lock.owner.prioritize()
    activeLocks.append(lock)
    unitsByLock(lock) = unitsByLock.getOrElse(lock, mutable.Set.empty)
  }

  private def recruitable(unit: FriendlyUnitInfo): Boolean = unit.alive && unit.unitClass.orderable && unit.remainingCompletionFrames < With.latency.remainingFrames
  
  private def lockTo(unit: FriendlyUnitInfo, lock: LockUnits): Unit = {
    unitsByLock(lock).add(unit)
    unlockedUnits.remove(unit)
  }
  
  private def unlock(unit: FriendlyUnitInfo): Unit = {
    unlockedUnits.add(unit)
    unitsByLock.find(_._2.contains(unit)).foreach(pair => unitsByLock(pair._1).remove(unit))
  }

  def audit: Vector[(Prioritized, mutable.Set[FriendlyUnitInfo])] = {
    unitsByLock.toVector.sortBy(_._1.owner.priorityUntouched).map(p => (p._1.owner, p._2))
  }
}
