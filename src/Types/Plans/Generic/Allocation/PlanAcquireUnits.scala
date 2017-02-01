package Types.Plans.Generic.Allocation

import Startup.With
import Types.Plans.Plan
import Types.Traits.UnitRequest
import bwapi.Unit

import scala.collection.mutable

abstract class PlanAcquireUnits extends Plan with UnitRequest {
  
  override def execute() {
    With.recruiter.add(this)
  }
  
  override def abort() {
    With.recruiter.remove(this)
  }
  
  def units():mutable.Set[Unit] = {
    With.recruiter.getUnits(this)
  }
  
  def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]]
}
