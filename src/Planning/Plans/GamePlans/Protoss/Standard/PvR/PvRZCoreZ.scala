package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpenZCoreZ

class PvRZCoreZ extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpenZCoreZ)
  override val completionCriteria = new UnitsAtLeast(2, Protoss.Zealot)
  override val buildOrder         = ProtossBuilds.ZCoreZ
}
