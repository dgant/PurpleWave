package Planning.Plans.Gameplans.Protoss

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Protoss.PvP.ProtossVsProtoss
import Planning.Plans.Gameplans.Protoss.PvR.ProtossVsRandom
import Planning.Plans.Gameplans.Protoss.PvT.ProtossVsTerran
import Planning.Plans.Gameplans.Protoss.PvZ.ProtossVsZerg

class ProtossStandardGameplan extends ModalGameplan(
  new ProtossVsRandom,
  new SwitchEnemyRace(
    new ProtossVsTerran,
    new ProtossVsProtoss,
    new ProtossVsZerg)
)
