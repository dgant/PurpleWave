package Planning.Plans

import Planning.Plans.GamePlans.Protoss.Standard.PvE.ProtossStandardGamePlan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TerranStandardGamePlan
import Planning.Plans.GamePlans.Zerg.GamePlans.ZergStandardGamePlan
import Planning.Plans.Predicates.Matchup.SwitchOurRace

class StandardGamePlan extends SwitchOurRace(
  whenTerran  = new TerranStandardGamePlan,
  whenProtoss = new ProtossStandardGamePlan,
  whenZerg    = new ZergStandardGamePlan)