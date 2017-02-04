package Plans.Generic.Allocation

import Startup.With
import Plans.Plan
import bwapi.Unit

import scala.collection.mutable

abstract class LockUnits extends Plan {
  
  var isSatisfied:Boolean = false
  
  override def isComplete: Boolean = { isSatisfied }
  
  override def onFrame() {
    With.recruiter.add(this)
  }
  
  def units:mutable.Set[Unit] = {
    With.recruiter.getUnits(this)
  }
  
  def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]]
}
