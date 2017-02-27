package Plans.Allocation

import Plans.Plan
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo

abstract class LockUnits extends Plan {
  
  var isSatisfied:Boolean = false
  
  override def isComplete: Boolean = { isSatisfied }
  
  override def onFrame() {
    With.recruiter.add(this)
  }
  
  def units:Set[FriendlyUnitInfo] = {
    With.recruiter.getUnits(this)
  }
  
  def getRequiredUnits(candidates:Iterable[Iterable[FriendlyUnitInfo]]):Option[Iterable[FriendlyUnitInfo]]
  
  override def toString: String = {
    super.toString + (if(isComplete) ": " + units.size else "")
  }
}
