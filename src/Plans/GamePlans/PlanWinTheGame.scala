package Plans.GamePlans

import Plans.GamePlans.Protoss.ProtossStrategyMacro
import Plans.Generic.Compound.AllParallel

class PlanWinTheGame extends AllParallel {
  children.set(List(new ProtossStrategyMacro))
}
