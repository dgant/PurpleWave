package Types.Plans.Strategy

import Types.Plans.Generic.Compound.PlanDelegateInParallel
import Types.Plans.Generic.Macro.{PlanFollowBuildOrder, PlanGatherMinerals}

class PlanWinTheGame extends PlanDelegateInParallel {
  _children = List(
    new PlanFollowBuildOrder,
    new PlanGatherMinerals
  )
}
