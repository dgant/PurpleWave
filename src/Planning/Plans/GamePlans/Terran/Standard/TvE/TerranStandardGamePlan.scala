package Planning.Plans.GamePlans.Terran.Standard.TvE

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvP.TerranVsProtoss
import Planning.Plans.GamePlans.Terran.Standard.TvR.TerranVsRandom
import Planning.Plans.GamePlans.Terran.Standard.TvT.TerranVsTerran
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TerranVsZerg

class TerranStandardGamePlan extends ModalGameplan(
  new TerranVsRandom,
  new SwitchEnemyRace(
    new TerranVsTerran,
    new TerranVsProtoss,
    new TerranVsZerg)
)
