package Planning.ResourceLocks

import Lifecycle.With
import Mathematics.Maff
import Planning.Prioritized
import Planning.UnitCounters.{CountEverything, CountUpTo, UnitCounter}
import Planning.UnitMatchers.{MatchAnything, UnitMatcher}
import Planning.UnitPreferences.{PreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class LockUnits(val owner: Prioritized) {
  var interruptable : Boolean         = true
  var matcher       : UnitMatcher     = MatchAnything
  var preference    : UnitPreference  = PreferAnything
  var counter       : UnitCounter     = CountEverything

  private var _isSatisfied: Boolean = false
  def satisfied: Boolean = _isSatisfied

  def acquire(prioritized: Prioritized): collection.Set[FriendlyUnitInfo] = {
    owner.prioritize()
    With.recruiter.satisfy(this)
    units
  }

  def inquire(prioritized: Prioritized): Option[Vector[FriendlyUnitInfo]] = {
    owner.prioritize()
    With.recruiter.inquire(this, isDryRun = true).map(_.toVector) // toVector ensures we don't return a view with invalid owner
  }

  def release() {
    With.recruiter.deactivate(this)
  }

  def units: collection.Set[FriendlyUnitInfo] = With.recruiter.lockedBy(this)

  // Invoked by Recruiter
  def offerUnits(candidates: Iterable[FriendlyUnitInfo], dryRun: Boolean): Option[Seq[FriendlyUnitInfo]] = {
    val finalists = findFinalists(candidates)
    val finalistsSatisfy = counter.accept(finalists)
    if ( ! dryRun) _isSatisfied = finalistsSatisfy
    if (finalistsSatisfy) Some(finalists) else None
  }

  private def findFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    // Here's a bunch of special-case performance shortcuts
    if (counter == CountEverything) {
      if (matcher == MatchAnything) {
        candidates.toSeq
      } else {
        candidates.filter(matcher).toSeq
      }
    } else if (counter == CountUpTo(1)) {
      findSingleFinalist(candidates)
    } else {
      findMultipleFinalists(candidates)
    }
  }

  private def findSingleFinalist(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    Maff.minBy(candidates.filter(matcher))(preference.apply).toSeq
  }

  private def findMultipleFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {

    val desiredUnits = new ArrayBuffer[FriendlyUnitInfo]()

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
    while (counter.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += dequeue()._1
    }
    desiredUnits
  }
}
