package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Planning.Plans.Compound.{Serial, SwitchEnemyRace}
import Planning.Plans.GamePlans.Protoss.Standard.PvP.ProtossVsProtoss
import Planning.Plans.GamePlans.Protoss.Standard.PvR.ProtossVsRandom
import Planning.Plans.GamePlans.Protoss.Standard.PvT.ProtossVsTerran
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.ProtossVsZerg

class ProtossStandardGamePlan extends Serial(
  new ProtossVsRandom,
  new SwitchEnemyRace(
    new ProtossVsTerran,
    new ProtossVsProtoss,
    new ProtossVsZerg)
)
