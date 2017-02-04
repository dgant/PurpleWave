package Plans.GamePlans.Protoss

import Plans.GamePlans.Protoss.Proxy.ProtossRushWithProxyZealots
import Plans.Generic.Compound.AllSimultaneous
import Plans.Generic.Macro.PlanGatherMinerals

class ProtossStrategyCheese extends AllSimultaneous {
  setChildren(List(
    new ProtossRushWithProxyZealots(),
    new PlanGatherMinerals()
  ))
}
