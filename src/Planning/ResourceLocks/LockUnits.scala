package Planning.ResourceLocks

import Planning.{Plan, Property}
import Lifecycle.With
import Planning.UnitCounters.{UnitCountEverything, UnitCountExactly, UnitCounter}
import Planning.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

class LockUnits extends {
  
  var canPoach          = new Property[Boolean](false)
  var interruptable     = new Property[Boolean](true)
  val unitMatcher       = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference    = new Property[UnitPreference](UnitPreferAnything)
  val unitCounter       = new Property[UnitCounter](UnitCountEverything)
  
  var owner: Plan = _
  
  var isSatisfied:Boolean = false
  def satisfied: Boolean = isSatisfied
  
  def acquire(plan: Plan) {
    owner = plan
    With.recruiter.add(this)
  }
  
  def inquire(plan: Plan): Option[Vector[FriendlyUnitInfo]] = {
    val ownerBefore = owner // Inquiring is supposed to be free of side-effects so retain the owner
    owner = plan
    val output = With.recruiter.inquire(this, isDryRun = true).map(_.toVector) // toVector ensures we don't return a view with invalid owner
    owner = ownerBefore
    output
  }
  
  def release() {
    With.recruiter.release(this)
  }
  
  def units: collection.Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)

  protected def weAccept(unit: FriendlyUnitInfo): Boolean = unitMatcher.get.apply(unit)

  def offerUnits(candidates: Iterable[FriendlyUnitInfo], dryRun: Boolean): Option[Seq[FriendlyUnitInfo]] = {
    unitCounter.get.reset()
    val finalists = findFinalists(candidates)
    val finalistsSatisfy = unitCounter.get.accept(finalists)
    if ( ! dryRun) isSatisfied = finalistsSatisfy
    if (finalistsSatisfy) Some(finalists) else None
  }

  private def findFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    // Here's a bunch of special-case performance shortcuts
    if (unitCounter.get == UnitCountEverything) {
      if (unitMatcher.get == UnitMatchAnything) {
        candidates.toSeq
      } else {
        candidates.filter(weAccept).toSeq
      }
    } else if (unitCounter.get == UnitCountExactly(1)) {
      findSingleFinalist(candidates)
    } else {
      findMultipleFinalists(candidates)
    }
  }

  protected def findSingleFinalist(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {
    ByOption.minBy(candidates.filter(weAccept))(unitPreference.get.apply).toSeq
  }

  protected def findMultipleFinalists(candidates: Iterable[FriendlyUnitInfo]): Seq[FriendlyUnitInfo] = {

    val desiredUnits = new mutable.ArrayBuffer[FriendlyUnitInfo]()

    // Build a queue based on whether we need to sort it
    val (candidateQueue, dequeue, preference) =
      if (unitPreference.get == UnitPreferAnything) {
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
            - unitPreference.get.apply(candidate)
            * (if (units.contains(candidate)) 1.0 else 1.5))
      }

    candidateQueue ++= candidates.filter(weAccept).map(c => (c, preference(c)))
    while (unitCounter.get.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += dequeue()._1
    }
    desiredUnits
  }
}
