package Planning.Plans.Allocation

import Planning.Plan
import Startup.With
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Composition.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class LockUnits extends Plan {
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference = new Property[UnitPreference](UnitPreferAnything)
  val unitCounter = new Property[UnitCounter](UnitCountEverything)
  
  var isSatisfied:Boolean = false
  
  description.set(
    if(isComplete)
      units
      .groupBy(_.unitClass)
      .map(pair => pair._1 + " " + pair._2.size)
      .mkString(", ")
    else
      "")
  
  override def isComplete: Boolean = isSatisfied
  override def onFrame() = With.recruiter.add(this)
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
