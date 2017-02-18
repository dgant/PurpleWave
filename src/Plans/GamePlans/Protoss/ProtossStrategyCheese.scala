package Plans.GamePlans.Protoss

import Plans.GamePlans.Protoss.Proxy.ProtossRushWithProxyZealots
import Plans.Compound.AllParallel
import Plans.Defense.DefeatWorkerHarass
import Plans.Macro.Automatic.{GatherGas, GatherMinerals}

class ProtossStrategyCheese extends AllParallel {
  children.set(List(
    new ProtossRushWithProxyZealots,
    new DefeatWorkerHarass,
    new GatherGas,
    new GatherMinerals
  ))
}
