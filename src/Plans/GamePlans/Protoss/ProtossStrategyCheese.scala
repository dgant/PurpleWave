package Plans.GamePlans.Protoss

import Plans.GamePlans.Protoss.Proxy.ProtossRushWithProxyZealots
import Plans.Generic.Compound.AllParallel
import Plans.Generic.Macro.PlanGatherMinerals

class ProtossStrategyCheese extends AllParallel {
  children.set(List(
    new ProtossRushWithProxyZealots(),
    new PlanGatherMinerals()
  ))
}
