package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpenZCoreZ

class PvRZCoreZ extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpenZCoreZ)
  override val completionCriteria = new UnitsAtLeast(2, Protoss.Zealot)
  override val buildOrder         = ProtossBuilds.OpeningZCoreZ
}
