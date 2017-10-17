package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvREarly2Gate1012

class PvR2Gate1012 extends GameplanModeTemplateVsRandom {
  
  override val completionCriteria = new UnitsAtLeast(2, Protoss.Zealot)
  
  override val activationCriteria = new Employing(PvREarly2Gate1012)
  override val buildOrder         = ProtossBuilds.OpeningTwoGate1012
  override def scoutAt            = 9
  override def defaultAttackPlan  = new Plan
  
  override def buildPlans = Vector(
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Protoss.Zealot, 4),
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)))
}
