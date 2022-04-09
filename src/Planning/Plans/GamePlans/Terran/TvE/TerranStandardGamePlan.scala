package Planning.Plans.GamePlans.Terran.TvE

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.TvP.TerranVsProtoss
import Planning.Plans.GamePlans.Terran.TvR.TerranVsRandom
import Planning.Plans.GamePlans.Terran.TvT.TerranVsTerran
import Planning.Plans.GamePlans.Terran.TvZ.TerranVsZerg

class TerranStandardGamePlan extends ModalGameplan(
  new TerranVsRandom,
  new SwitchEnemyRace(
    new TerranVsTerran,
    new TerranVsProtoss,
    new TerranVsZerg)
)
