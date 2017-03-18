package Planning.Plans.GamePlans

import Planning.Plans.Army.Defend
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace
import Planning.Plans.Macro.Automatic.{GatherGas, GatherMinerals}
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder

class ProtossGamePlan extends Parallel {
  children.set(List(
    new SwitchEnemyRace {
      terran  .set(new ProtossVsTerran)
      protoss .set(new ProtossVsProtoss)
      zerg    .set(new ProtossVsZerg)
      random  .set(new ProtossVsRandom)
    },
    new FollowBuildOrder,
    new GatherGas,
    new GatherMinerals,
    new Defend
  ))
}
