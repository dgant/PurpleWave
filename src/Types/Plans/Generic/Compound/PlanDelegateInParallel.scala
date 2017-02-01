package Types.Plans.Generic.Compound

import Types.Plans.Plan

class PlanDelegateInParallel extends Plan {
  
  override def isComplete():Boolean = {
    children.forall(_.isComplete)
  }
  
  override def execute() = {
    children.foreach(_.execute())
  }
}
