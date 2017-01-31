package Development

import Startup.With
import Types.Plans.Plan

object Overlay {
  def update() {
    With.game.drawTextScreen(
      5, 5,
      With.planner.topLevelPlans().map(_describePlanTree(_, 0)).mkString(""))
  }
  
  def _describePlanTree(plan:Plan, depth:Integer):String = {
    _describePlan(plan, depth) + plan.children.map(_describePlanTree(_, depth + 1)).mkString("")
  }
  
  def _describePlan(plan:Plan, depth:Integer):String = {
    (" " * depth
      ++ "+"
      ++ plan.getClass.getSimpleName
      ++ " ("
      ++ (if(plan.active) "active" else "inactive")
      ++ ", "
      ++ (if(plan.isComplete) "complete" else "incomplete")
      ++ ")\n"
      )
  }
}
