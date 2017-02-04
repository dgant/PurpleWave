package Plans.Generic.Compound

import Plans.Plan
import Traits.Property

abstract class AbstractPlanCompleteAll extends Plan {
  
  val children = new Property[List[Plan]](List.empty)
  
  final override def isComplete:Boolean = {
    getChildren.forall(_.isComplete)
  }
  
  final override def getChildren: Iterable[Plan] = {
    children.get
  }
}
