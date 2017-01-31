package Processes

import Types.Plans.{Plan, PlanFollowBuildOrder, PlanGatherMinerals}

class Planner {
  
  val _defaultPlans:List[Plan] = List(
    new PlanFollowBuildOrder,
    new PlanGatherMinerals
  )
  _defaultPlans.foreach(_.initialize())

  def plans(): Iterable[Plan] = {
    _defaultPlans.flatten(_flattenActivePlans)
  }
  
  def _flattenActivePlans(plan:Plan):Iterable[Plan] = {
    Iterable(plan) ++ plan.children().flatten(_flattenActivePlans)
  }
}