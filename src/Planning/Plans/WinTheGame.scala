package Planning.Plans

import Planning.Plans.Information.SwitchOurRace
import Planning.Plans.Protoss.GamePlans.ProtossStandardGamePlan
import Planning.Plans.Terran.GamePlans.TerranStandardGamePlan
import Planning.Plans.Zerg.GamePlans.ZergGamePlan

class WinTheGame extends SwitchOurRace(
  whenTerran  = new TerranStandardGamePlan,
  whenProtoss = new ProtossStandardGamePlan,
  whenZerg    = new ZergGamePlan)