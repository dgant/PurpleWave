package Development

import Plans.Plan
import Startup.With

object Debugger {
  
  def plans:Iterable[Plan] = {
    _flatten(With.gameplan)
  }
  
  def _flatten(plan:Plan):Iterable[Plan] = {
    List(plan) ++ plan.getChildren.flatten(_flatten)
  }
  
}
