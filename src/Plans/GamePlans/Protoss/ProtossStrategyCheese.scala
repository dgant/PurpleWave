package Plans.GamePlans.Protoss

import Plans.GamePlans.Protoss.Proxy.ProtossRushWithProxyZealots
import Plans.Generic.Compound.Simultaneous
import Plans.Generic.Macro.PlanGatherMinerals

class ProtossStrategyCheese extends Simultaneous {
  setChildren(List(
    new ProtossRushWithProxyZealots(),
    new PlanGatherMinerals()
  ))
}
