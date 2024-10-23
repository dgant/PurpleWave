package Planning.ResourceLocks

import Lifecycle.With
import Macro.Allocation.Prioritized
import Mathematics.Maff
import Utilities.UnitCounters.{CountEverything, CountExactly, CountUpTo, UnitCounter}
import Utilities.UnitFilters.{IsAnything, UnitFilter}
import Utilities.UnitPreferences.{PreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class LockUnits(
  val owner         : Prioritized,
  var matcher       : UnitFilter      = IsAnything,
  var preference    : UnitPreference  = PreferAnything,
  var counter       : UnitCounter     = CountEverything,
  var interruptable : Boolean         = true) {

  def this(owner: Prioritized, matcher: UnitFilter, counter: UnitCounter, interruptable: Boolean) {
    this(owner, matcher)
    setCounter(counter)
    setInterruptible(interruptable)
  }

  def this(owner: Prioritized, matcher: UnitFilter, counter: UnitCounter) {
    this(owner, matcher)
    setCounter(counter)
  }

  private var _isSatisfied: Boolean = false
  def satisfied: Boolean = _isSatisfied

  def acquire(): collection.Set[FriendlyUnitInfo] = {
    owner.prioritize()
    With.recruiter.satisfy(this)
    units
  }

  def reacquire(): collection.Set[FriendlyUnitInfo] = {
    release()
    acquire()
  }

  def inquire(): Option[Vector[FriendlyUnitInfo]] = {
    owner.prioritize()
    With.recruiter.inquire(this, isDryRun = true).map(_.toVector) // toVector ensures we don't return a view with invalid owner
  }

  def release(): LockUnits = {
    With.recruiter.deactivate(this)
    this
  }

  def units: collection.Set[FriendlyUnitInfo] = With.recruiter.lockedBy(this)

  def setInterruptible  (value: Boolean)        : LockUnits = { interruptable = value; this }
  def setMatcher        (value: UnitFilter)     : LockUnits = { matcher       = value; this }
  def setPreference     (value: UnitPreference) : LockUnits = { preference    = value; this }
  def setCounter        (value: UnitCounter)    : LockUnits = { counter       = value; this }

  // Invoked by Recruiter
  def offerUnits(candidates: Iterable[FriendlyUnitInfo], dryRun: Boolean): Option[Seq[FriendlyUnitInfo]] = {
    val finalists = findFinalists(candidates)
    val finalistsSatisfy = counter.accept(finalists)
    if ( ! dryRun) _isSatisfied = finalistsSatisfy
    ?(finalistsSatisfy, Some(finalists), None)
  }

  //noinspection ComparingUnrelatedTypes
  private def findFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    // Here's a bunch of special-case performance shortcuts
    counter match {
      case CountUpTo(0) | CountExactly(0) => Seq.empty
      case CountUpTo(1) | CountExactly(1) => Maff.minBy(candidates.filter(matcher))(preference.apply).toSeq
      case CountEverything                => ?(matcher == IsAnything, candidates, candidates.filter(matcher)).toSeq
      case _                              => findMultipleFinalists(candidates)
    }
  }

  //noinspection ComparingUnrelatedTypes
  private def findMultipleFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    // Build a queue based on whether we need to sort it
    val (candidateQueue, dequeue, preference) =
      if (this.preference == PreferAnything) {
        val output = new mutable.Queue[(FriendlyUnitInfo, Double)]()
        (
          output,
          () => output.dequeue(),
          (candidate: FriendlyUnitInfo) => 0.0)
      } else {
        val output = new mutable.PriorityQueue[(FriendlyUnitInfo, Double)]()(Ordering.by(_._2))
        (
          output,
          () => output.dequeue(),
          (candidate: FriendlyUnitInfo) =>
            - this.preference(candidate)
            * (if (units.contains(candidate)) 1.0 else 1.5))
      }
    candidateQueue ++= candidates.filter(matcher).map(c => (c, preference(c)))
    val desiredUnits = new ArrayBuffer[FriendlyUnitInfo]()
    while (counter.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += dequeue()._1
    }
    desiredUnits
  }
}
