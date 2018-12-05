package Planning.ResourceLocks

import Planning.{Plan, Property}
import Lifecycle.With
import Planning.Composition.UnitCountEverything
import Planning.UnitCounters.{UnitCountExactly, UnitCounter}
import Planning.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

class LockUnits extends {
  
  var canPoach          = new Property[Boolean](false)
  var interruptable     = new Property[Boolean](true)
  var acceptSubstitutes = new Property[Boolean](false)
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
  
  def inquire(plan: Plan): Option[Iterable[FriendlyUnitInfo]] = {
    val ownerBefore = owner // This is supposed to be free of side-effects so retain the owner
    owner = plan
    val output = With.recruiter.inquire(this, isDryRun = true)
    owner = ownerBefore
    output
  }
  
  def release() {
    With.recruiter.release(this)
  }
  
  def units: collection.Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)

  protected def weAccept(unit: FriendlyUnitInfo): Boolean = {
    if(acceptSubstitutes.get)
      unitMatcher.get.acceptAsPrerequisite(unit)
    else
      unitMatcher.get.accept(unit)
  }

  def offerUnits(candidates: Iterable[FriendlyUnitInfo], dryRun: Boolean): Option[Iterable[FriendlyUnitInfo]] = {
    unitCounter.get.reset()
    val finalists = findFinalists(candidates)
    val finalistsSatisfy = unitCounter.get.accept(finalists)
    if ( ! dryRun) isSatisfied = finalistsSatisfy
    if (finalistsSatisfy) Some(finalists) else None
  }

  def findFinalists(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    // Here's a bunch of special-case performance improvements.
    // offerMultipleUnits()
    if (unitCounter.get == UnitCountEverything) {
      if (unitMatcher.get == UnitMatchAnything) {
        candidates
      }
      else {
        candidates.filter(weAccept)
      }
    }
    else if (unitCounter.get == UnitCountExactly(1)) {
      findSingleFinalist(candidates)
    }
    else {
      findMultipleFinalists(candidates)
    }
  }

  protected def findSingleFinalist(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    ByOption.minBy(candidates.filter(weAccept))(unitPreference.get.preference)
  }

  protected def findMultipleFinalists(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {

    val desiredUnits = With.recruiter.getUnits(this).to[mutable.Set]

    // Build a queue based on whether we ned to sort it
    val (candidateQueue, dequeue) =
      if (unitPreference.get == UnitPreferAnything) {
        val output = new mutable.Queue[FriendlyUnitInfo]()
        (output, () => output.dequeue())
      } else {
        val output = new mutable.PriorityQueue[FriendlyUnitInfo]()(Ordering.by( - unitPreference.get.preference(_))) // Negative because priority queue is highest-first
        (output, () => output.dequeue())
      }

    candidateQueue ++= candidates.filter(weAccept)
    while (unitCounter.get.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += dequeue()
    }
    desiredUnits
  }
}
