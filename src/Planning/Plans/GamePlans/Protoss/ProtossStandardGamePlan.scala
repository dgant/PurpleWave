package Planning.Plans.GamePlans.Protoss

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Protoss.PvP.ProtossVsProtoss
import Planning.Plans.GamePlans.Protoss.PvR.ProtossVsRandom
import Planning.Plans.GamePlans.Protoss.PvT.ProtossVsTerran
import Planning.Plans.GamePlans.Protoss.PvZ.ProtossVsZerg

class ProtossStandardGamePlan extends ModalGameplan(
  new ProtossVsRandom,
  new SwitchEnemyRace(
    new ProtossVsTerran,
    new ProtossVsProtoss,
    new ProtossVsZerg)
)
