package Plans.GamePlans.Protoss

import Plans.GamePlans.Protoss.Proxy.ProtossRushWithProxyZealots
import Plans.Generic.Compound.AllParallel
import Plans.Generic.Defense.DefeatWorkerHarass
import Plans.Generic.Macro.GatherMinerals

class ProtossStrategyCheese extends AllParallel {
  children.set(List(
    new ProtossRushWithProxyZealots,
    new DefeatWorkerHarass,
    new GatherMinerals
  ))
}
