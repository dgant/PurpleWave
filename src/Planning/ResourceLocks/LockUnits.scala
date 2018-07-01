package Planning.ResourceLocks

import Planning.{Plan, Property}
import Lifecycle.With
import Planning.Composition.UnitCountEverything
import Planning.UnitCounters.UnitCounter
import Planning.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

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
    val ownerBefore = owner //This is supposed to be free of side-effects so retain the owner
    owner = plan
    val output = With.recruiter.inquire(this)
    owner = ownerBefore
    output
  }
  
  def release() {
    With.recruiter.release(this)
  }
  
  def units: Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)
  
  def offerUnits(candidates: Iterable[FriendlyUnitInfo]): Option[Iterable[FriendlyUnitInfo]] = {
  
    val desiredUnits    = With.recruiter.getUnits(this).to[mutable.Set]
    val candidateQueue  = new mutable.PriorityQueue[FriendlyUnitInfo]()(Ordering.by( - unitPreference.get.preference(_))) //Negative because priority queue is highest-first
    candidateQueue ++= candidates.filter(c =>
      if(acceptSubstitutes.get)
        unitMatcher.get.acceptAsPrerequisite(c)
      else
        unitMatcher.get.accept(c))
    
    unitCounter.get.reset()
    
    while (unitCounter.get.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += candidateQueue.dequeue()
    }
    
    isSatisfied = unitCounter.get.accept(desiredUnits)
    if (isSatisfied)
      Some(desiredUnits)
    else
      None
  }
}
