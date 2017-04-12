package Planning.Plans.GamePlans

import Planning.Plans.Army.Defend
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder
import Planning.Plans.Macro.RemoveMineralBlockAt

class ProtossGamePlan extends Parallel {
  children.set(Vector(
    new SwitchEnemyRace {
      terran  .set(new ProtossVsTerran)
      protoss .set(new ProtossVsProtoss)
      zerg    .set(new ProtossVsZerg)
      random  .set(new ProtossVsRandom)
    },
    new FollowBuildOrder,
    new RemoveMineralBlockAt(30),
    new Gather,
    new Defend
  ))
}
