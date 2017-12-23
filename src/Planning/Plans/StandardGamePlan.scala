package Planning.Plans

import Planning.Plans.GamePlans.Protoss.Standard.PvE.ProtossStandardGamePlan
import Planning.Plans.Information.SwitchOurRace
import Planning.Plans.GamePlans.Terran.TvE.TerranStandardGamePlan
import Planning.Plans.GamePlans.Zerg.GamePlans.ZergStandardGamePlan

class StandardGamePlan extends SwitchOurRace(
  whenTerran  = new TerranStandardGamePlan,
  whenProtoss = new ProtossStandardGamePlan,
  whenZerg    = new ZergStandardGamePlan)