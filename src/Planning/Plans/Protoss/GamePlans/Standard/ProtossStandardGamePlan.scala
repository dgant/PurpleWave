package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.Compound.Serial
import Planning.Plans.Information.SwitchEnemyRace

class ProtossStandardGamePlan extends Serial(
  new ProtossVsRandom,
  new SwitchEnemyRace(
    new ProtossVsTerran,
    new ProtossVsProtoss,
    new ProtossVsZerg)
)
