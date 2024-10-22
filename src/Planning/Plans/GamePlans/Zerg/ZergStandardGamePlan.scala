package Planning.Plans.Gameplans.Zerg

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Zerg.ZvP.ZergVsProtoss
import Planning.Plans.Gameplans.Zerg.ZvR.ZergVsRandom
import Planning.Plans.Gameplans.Zerg.ZvT.ZergVsTerran
import Planning.Plans.Gameplans.Zerg.ZvZ.ZergVsZerg

class ZergStandardGameplan extends ModalGameplan(
  new ZergVsRandom,
  new SwitchEnemyRace(
    new ZergVsTerran,
    new ZergVsProtoss,
    new ZergVsZerg)
)