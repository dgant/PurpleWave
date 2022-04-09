package Planning.Plans.GamePlans.Zerg

import Planning.Plans.Compound.SwitchEnemyRace
import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Zerg.ZvE.ZvE4Pool
import Planning.Plans.GamePlans.Zerg.ZvP.ZergVsProtoss
import Planning.Plans.GamePlans.Zerg.ZvT.ZergVsTerran
import Planning.Plans.GamePlans.Zerg.ZvZ.ZergVsZerg

class ZergStandardGamePlan extends ModalGameplan(
  new ZvE4Pool,
  new SwitchEnemyRace(
    new ZergVsTerran,
    new ZergVsProtoss,
    new ZergVsZerg)
)