package Plans.Allocation

import Plans.Plan
import Startup.With
import bwapi.Unit

abstract class LockUnits extends Plan {
  
  var isSatisfied:Boolean = false
  
  override def isComplete: Boolean = { isSatisfied }
  
  override def onFrame() {
    With.recruiter.add(this)
  }
  
  def units:Set[Unit] = {
    With.recruiter.getUnits(this)
  }
  
  def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]]
  
  override def toString: String = {
    super.toString + (if(isComplete) ": " + units.size else "")
  }
}
