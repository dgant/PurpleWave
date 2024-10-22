package Planning.Plans.Gameplans.Terran

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Terran.TvP.TerranVsProtoss
import Planning.Plans.Gameplans.Terran.TvR.TerranVsRandom
import Planning.Plans.Gameplans.Terran.TvT.TerranVsTerran
import Planning.Plans.Gameplans.Terran.TvZ.TerranVsZerg

class TerranStandardGameplan extends ModalGameplan(
  new TerranVsRandom,
  new SwitchEnemyRace(
    new TerranVsTerran,
    new TerranVsProtoss,
    new TerranVsZerg)
)
