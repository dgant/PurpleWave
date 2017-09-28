package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace

class ProtossStandardGamePlan
  extends Parallel (
    new SwitchEnemyRace {
      terran  .set(new ProtossVsTerran)
      protoss .set(new ProtossVsProtoss)
      zerg    .set(new ProtossVsZerg)
      random  .set(new ProtossVsRandom)
    }
  )
