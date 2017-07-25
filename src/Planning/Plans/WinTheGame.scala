package Planning.Plans

import Planning.Plans.Information.SwitchOurRace
import Planning.Plans.Protoss.GamePlans.ProtossGamePlan
import Planning.Plans.Zerg.GamePlans.ZergGamePlan

class WinTheGame extends SwitchOurRace(
  whenProtoss = new ProtossGamePlan,
  whenZerg    = new ZergGamePlan)