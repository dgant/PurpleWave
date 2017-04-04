package Debugging

import Planning.Plan
import Lifecycle.With

object Debugger {
  
  def plans:Iterable[Plan] = flatten(With.gameplan)
  
  def planDescriptions:Iterable[String] = plans.map(plan => plan.toString)
  
  private def flatten(plan:Plan):Iterable[Plan] = List(plan) ++ plan.getChildren.flatten(flatten)
}
