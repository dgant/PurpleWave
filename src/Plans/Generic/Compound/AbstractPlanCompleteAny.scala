package Plans.Generic.Compound

import Plans.Plan
import Traits.TraitSettableChildren

abstract class AbstractPlanCompleteAny
  extends Plan
  with TraitSettableChildren {
  
  override def isComplete():Boolean = {
    children.exists(_.isComplete)
  }
  
  def _dispatchIfAnyChildIsComplete() {
    if (children.exists(_.isComplete)) {
      onAnyChildComplete()
    }
  }
  
  def onAnyChildComplete() {}
}
