package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpenZZCore

class PvRZZCore extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpenZZCore)
  override val completionCriteria = new UnitsAtLeast(1, Protoss.CyberneticsCore)
  override val buildOrder         = ProtossBuilds.ZZCore
}
