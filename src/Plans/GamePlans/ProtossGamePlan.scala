package Plans.GamePlans

import Plans.Army.Defend
import Plans.Compound.Parallel
import Plans.Defense.DefeatWorkerHarass
import Plans.Information.SwitchEnemyRace
import Plans.Macro.Automatic.{GatherGas, GatherMinerals}
import Plans.Macro.Build.FollowBuildOrder

class ProtossGamePlan extends Parallel {
  children.set(List(
    new SwitchEnemyRace {
      terran  .set(new ProtossVsTerran)
      protoss .set(new ProtossVsProtoss)
      zerg    .set(new ProtossVsZerg)
      random  .set(new ProtossVsRandom)
    },
    new FollowBuildOrder,
    new DefeatWorkerHarass,
    new GatherGas,
    new GatherMinerals,
    new Defend
  ))
}
