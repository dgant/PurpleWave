package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpenZZCore

class PvRZZCore extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpenZZCore)
  override val completionCriteria = new UnitsAtLeast(1, Protoss.CyberneticsCore)
  override val buildOrder         = ProtossBuilds.OpeningZZCore
}
