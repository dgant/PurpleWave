package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpen2Gate1012

class PvR2Gate1012 extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria = new Employing(PvROpen2Gate1012)
  override val completionCriteria = new UnitsAtLeast(2, Protoss.Zealot)
  override val buildOrder         = ProtossBuilds.OpeningTwoGate1012
  override def scoutAt            = 9
  override def defaultAttackPlan  = new Plan
  
  override def buildPlans = Vector(
    new Pump(Protoss.Zealot, 4),
    new Build(
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)))
}
