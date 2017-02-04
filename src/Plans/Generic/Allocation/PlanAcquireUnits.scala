package Plans.Generic.Allocation

import Startup.With
import Plans.Plan
import Traits.{TraitSettableSatisfaction, TraitSettableUnitMatcher, TraitSettableUnitPreference}
import bwapi.Unit

import scala.collection.mutable

abstract class PlanAcquireUnits
  extends Plan
  with TraitSettableSatisfaction
  with TraitSettableUnitMatcher
  with TraitSettableUnitPreference {
  
  override def isComplete(): Boolean = { getSatisfaction }
  
  override def onFrame() {
    With.recruiter.add(this)
  }
  
  def units:mutable.Set[Unit] = {
    With.recruiter.getUnits(this)
  }
  
  def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]]
}
