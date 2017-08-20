package Planning.Plans.Terran.GamePlans

import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.SwitchEnemyRace

class TerranStandardGamePlan
  extends Parallel (
    new SwitchEnemyRace {
      terran  .set(new TerranVsTerran)
      protoss .set(new ProxyBBS)
      zerg    .set(new TerranVsZergEarly)
      random  .set(new TerranVsZergEarly)
    }
  )
