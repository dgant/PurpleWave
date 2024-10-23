package Gameplans.Zerg

import Gameplans.All.ModalGameplan
import Gameplans.Zerg.ZvP.ZergVsProtoss
import Gameplans.Zerg.ZvR.ZergVsRandom
import Gameplans.Zerg.ZvT.ZergVsTerran
import Gameplans.Zerg.ZvZ.ZergVsZerg
import Planning.Plans.SwitchEnemyRace

class ZergStandardGameplan extends ModalGameplan(
  new ZergVsRandom,
  new SwitchEnemyRace(
    new ZergVsTerran,
    new ZergVsProtoss,
    new ZergVsZerg)
)