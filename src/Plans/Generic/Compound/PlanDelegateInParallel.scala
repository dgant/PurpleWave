package Plans.Generic.Compound

import Plans.Plan

class PlanDelegateInParallel extends Plan {
  
  override def isComplete():Boolean = {
    children.forall(_.isComplete)
  }
  
  override def execute() = {
    children.foreach(_.execute())
  }
}
