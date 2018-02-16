package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound.{And, NoPlan}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarlyNexusFirst

class PvT13Nexus extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarlyNexusFirst)
  override val completionCriteria = new And(new UnitsAtLeast(4, Protoss.Gateway), new UnitsAtLeast(1, Protoss.Observatory))
  override val aggression         = 0.6
  override val defaultAttackPlan  = NoPlan()
  override val buildOrder         = ProtossBuilds.Opening13Nexus
  override val scoutAt            = 14
  
  override val buildPlans = Vector(
    new TrainContinuously(Protoss.Observer, 1),
    new TrainContinuously(Protoss.Dragoon),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(4, Protoss.Gateway)))
}

