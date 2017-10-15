package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Planning.Plans.Compound.And
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvREarlyZZCore

class PvRZZCore extends PvR2Gate1012 {
  
  override val completionCriteriaAdditional = new And(new UnitsAtLeast(1, Protoss.CyberneticsCore))
  
  override val activationCriteria = new Employing(PvREarlyZZCore)
  override val buildOrder         = ProtossBuilds.OpeningZZCore
}
