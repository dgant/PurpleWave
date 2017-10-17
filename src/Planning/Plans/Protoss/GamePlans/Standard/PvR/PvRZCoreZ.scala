package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvREarlyZCoreZ

class PvRZCoreZ extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvREarlyZCoreZ)
  override val completionCriteria = new UnitsAtLeast(2, Protoss.Zealot)
  override val buildOrder         = ProtossBuilds.OpeningZCoreZ
}
