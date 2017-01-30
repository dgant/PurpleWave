package Processes

import Types.Plans.{Plan, PlanFollowBuildOrder, PlanGatherMinerals}

class Planner {
  val _defaultPlans:List[Plan] = List(
    new PlanGatherMinerals,
    new PlanFollowBuildOrder
  )

  def plans(): Iterable[Plan] = {
    _defaultPlans.flatten(_flatten)
  }
  
  def _flatten(plan:Plan):Iterable[Plan] = {
    Iterable(plan) ++ plan.children().flatten(_flatten)
  }
}