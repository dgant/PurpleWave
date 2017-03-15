package Plans.Allocation

import Development.TypeDescriber
import Plans.Plan
import Startup.With
import Strategies.UnitCountEverything
import Strategies.UnitCounters.UnitCounter
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferAnything, UnitPreference}
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property

import scala.collection.mutable

class LockUnits extends Plan {
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference = new Property[UnitPreference](UnitPreferAnything)
  val unitCounter = new Property[UnitCounter](UnitCountEverything)
  
  var isSatisfied:Boolean = false
  
  description.set(
    if(isComplete)
      units
      .groupBy(_.utype)
      .map(pair => TypeDescriber.unit(pair._1) + " " + pair._2.size)
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
