package Planning.Plans.Protoss.GamePlans

import Planning.Plans.Army.{DefendZones, Recruit}
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt

class ProtossGamePlan
  extends Parallel (
    new SwitchEnemyRace {
      terran  .set(new ProtossVsTerran)
      protoss .set(new ProtossVsProtoss)
      zerg    .set(new ProtossVsZerg)
      random  .set(new ProtossVsRandom)
    },
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(30),
    new Gather,
    new DefendZones,
    new Recruit
  )
