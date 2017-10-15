package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound.NoPlan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarlyNexusFirst

class PvT13Nexus extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarlyNexusFirst)
  override val completionCriteria = new UnitsAtLeast(4, Protoss.Dragoon)
  override val aggression         = 0.8
  override val defaultAttackPlan  = NoPlan()
  override val buildOrder         = ProtossBuilds.Opening13Nexus_Long
  
  override val buildPlans = Vector(
    new TrainContinuously(Protoss.Observer, 1),
    new TrainContinuously(Protoss.Dragoon),
    new PvTIdeas.BuildSecondGasIfWeNeedIt,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(3, Protoss.Gateway)))
}

