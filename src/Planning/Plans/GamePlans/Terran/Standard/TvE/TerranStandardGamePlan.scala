package Planning.Plans.GamePlans.Terran.Standard.TvE

import Planning.Plans.Compound.Serial
import Planning.Plans.GamePlans.Terran.Standard.TvP.TerranVsProtoss
import Planning.Plans.GamePlans.Terran.Standard.TvT.TerranVsTerran
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TerranVsZerg
import Planning.Plans.Information.SwitchEnemyRace

class TerranStandardGamePlan extends Serial(
  new TerranVsRandom,
  new SwitchEnemyRace(
    new TerranVsTerran,
    new TerranVsProtoss,
    new TerranVsZerg)
)
