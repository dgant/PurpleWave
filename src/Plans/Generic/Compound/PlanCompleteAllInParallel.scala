package Plans.Generic.Compound

class PlanCompleteAllInParallel extends PlanWithSettableListOfChildren {
  
  override def isComplete():Boolean = {
    children.forall(_.isComplete)
  }
  
  override def execute() = {
    children.foreach(_.execute())
  }
}
