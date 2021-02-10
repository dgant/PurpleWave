package Planning.ResourceLocks

import Lifecycle.With
import Planning.UnitCounters.{CountEverything, CountUpTo, UnitCounter}
import Planning.UnitMatchers.{MatchAnything, UnitMatcher}
import Planning.UnitPreferences.{PreferAnything, UnitPreference}
import Planning.{Prioritized, Property}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

class LockUnits {
  
  var canPoach      = new Property[Boolean](false)
  var interruptable = new Property[Boolean](true)
  val matcher       = new Property[UnitMatcher](MatchAnything)
  val preference    = new Property[UnitPreference](PreferAnything)
  val counter       = new Property[UnitCounter](CountEverything)

  var owner: Prioritized = _

  var isSatisfied:Boolean = false
  def satisfied: Boolean = isSatisfied

  def acquire(prioritized: Prioritized) {
    owner = prioritized
    owner.prioritize()
    With.recruiter.add(this)
  }

  def inquire(prioritized: Prioritized): Option[Vector[FriendlyUnitInfo]] = {
    owner = prioritized
    owner.prioritize()
    With.recruiter.inquire(this, isDryRun = true).map(_.toVector) // toVector ensures we don't return a view with invalid owner
  }

  def release() {
    With.recruiter.release(this)
  }

  def units: collection.Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)

  protected def weAccept(unit: FriendlyUnitInfo): Boolean = matcher.get.apply(unit)

  def offerUnits(candidates: Iterable[FriendlyUnitInfo], dryRun: Boolean): Option[Seq[FriendlyUnitInfo]] = {
    val finalists = findFinalists(candidates)
    val finalistsSatisfy = counter.get.accept(finalists)
    if ( ! dryRun) isSatisfied = finalistsSatisfy
    if (finalistsSatisfy) Some(finalists) else None
  }

  private def findFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    // Here's a bunch of special-case performance shortcuts
    if (counter.get == CountEverything) {
      if (matcher.get == MatchAnything) {
        candidates.toSeq
      } else {
        candidates.filter(weAccept).toSeq
      }
    } else if (counter.get == CountUpTo(1)) {
      findSingleFinalist(candidates)
    } else {
      findMultipleFinalists(candidates)
    }
  }

  protected def findSingleFinalist(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    ByOption.minBy(candidates.filter(weAccept))(preference.get.apply).toSeq
  }

  protected def findMultipleFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {

    val desiredUnits = new mutable.ArrayBuffer[FriendlyUnitInfo]()

    // Build a queue based on whether we need to sort it
    val (candidateQueue, dequeue, preference) =
      if (this.preference.get == PreferAnything) {
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
            - this.preference.get.apply(candidate)
            * (if (units.contains(candidate)) 1.0 else 1.5))
      }

    candidateQueue ++= candidates.filter(weAccept).map(c => (c, preference(c)))
    while (counter.get.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += dequeue()._1
    }
    desiredUnits
  }
}
