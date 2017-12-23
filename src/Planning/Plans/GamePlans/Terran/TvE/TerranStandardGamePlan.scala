package Planning.Plans.GamePlans.Terran.TvE

import Planning.Plans.Compound.Parallel
import Planning.Plans.GamePlans.Terran.TvT.TerranVsTerran
import Planning.Plans.GamePlans.Terran.TvZ.TerranVsZergEarly
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
