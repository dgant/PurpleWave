package Planning.Composition.ResourceLocks

import Planning.Plan
import Startup.With
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Composition.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class LockUnits extends ResourceLock {
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference = new Property[UnitPreference](UnitPreferAnything)
  val unitCounter = new Property[UnitCounter](UnitCountEverything)
  
  var owner:Plan = null
  
  var isSatisfied:Boolean = false
  override def isComplete: Boolean = isSatisfied
  override def acquire(plan:Plan) = {
    owner = plan
    With.recruiter.add(this)
  }
  
  override def release() {
    throw new NotImplementedError
  }
  
  def units:Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)
  
  def offerUnits(candidates:Iterable[Iterable[FriendlyUnitInfo]]):Option[Iterable[FriendlyUnitInfo]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).to[mutable.Set]
    
    candidates
      .flatten
      .toList
      .filter(unitMatcher.get.accept)
        .sortBy(unitPreference.get.preference)
        .foreach(unit => if (unitCounter.get.continue(desiredUnits)) desiredUnits.add(unit))
  
    isSatisfied = unitCounter.get.accept(desiredUnits)
    if (isSatisfied) Some(desiredUnits) else None
  }
  
  
}
