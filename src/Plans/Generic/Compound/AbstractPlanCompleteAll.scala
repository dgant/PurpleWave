package Plans.Generic.Compound

import Plans.Plan
import Traits.TraitSettableChildren

abstract class AbstractPlanCompleteAll
  extends Plan
  with TraitSettableChildren {
  
  final override def isComplete():Boolean = {
    children.forall(_.isComplete)
  }
}
