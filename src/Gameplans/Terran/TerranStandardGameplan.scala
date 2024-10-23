package Gameplans.Terran

import Gameplans.All.ModalGameplan
import Gameplans.Terran.TvP.TerranVsProtoss
import Gameplans.Terran.TvR.TerranVsRandom
import Gameplans.Terran.TvT.TerranVsTerran
import Gameplans.Terran.TvZ.TerranVsZerg
import Planning.Plans.SwitchEnemyRace

class TerranStandardGameplan extends ModalGameplan(
  new TerranVsRandom,
  new SwitchEnemyRace(
    new TerranVsTerran,
    new TerranVsProtoss,
    new TerranVsZerg)
)
