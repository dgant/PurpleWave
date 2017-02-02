package Plans.Strategy

import Plans.Generic.Compound.PlanDelegateInParallel
import Plans.Generic.Macro.{PlanFollowBuildOrder, PlanGatherMinerals}

class PlanWinTheGame extends PlanDelegateInParallel {
  _children = List(
    new PlanFollowBuildOrder,
    new PlanGatherMinerals
  )
}
