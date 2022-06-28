package Planning.Plans.GamePlans.All

import Planning.Plans.Compound.SwitchOurRace
import Planning.Plans.GamePlans.Protoss.ProtossStandardGamePlan
import Planning.Plans.GamePlans.Terran.TvE.TerranStandardGamePlan
import Planning.Plans.GamePlans.Zerg.ZergStandardGamePlan

class StandardGamePlan extends SwitchOurRace(
  whenTerran  = new TerranStandardGamePlan,
  whenProtoss = new ProtossStandardGamePlan,
  whenZerg    = new ZergStandardGamePlan)