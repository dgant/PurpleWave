package Planning.Plans.GamePlans

import Planning.Plans.Compound.SwitchOurRace
import Planning.Plans.GamePlans.Protoss.ProtossStandardGamePlan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TerranStandardGamePlan
import Planning.Plans.GamePlans.Zerg.GamePlans.ZergStandardGamePlan

class StandardGamePlan extends SwitchOurRace(
  whenTerran  = new TerranStandardGamePlan,
  whenProtoss = new ProtossStandardGamePlan,
  whenZerg    = new ZergStandardGamePlan)