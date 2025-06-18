package Gameplans.Protoss

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvP.ProtossVsProtoss
import Gameplans.Protoss.PvR.ProtossVsRandom
import Gameplans.Protoss.PvT.ProtossVsTerran
import Gameplans.Protoss.PvZ.ProtossVsZerg
import Planning.Plans.SwitchEnemyRace

class ProtossStandardGameplan extends ModalGameplan(
  new SwitchEnemyRace(
    new ProtossVsTerran,
    new ProtossVsProtoss,
    new ProtossVsZerg,
    new ProtossVsRandom)
)
