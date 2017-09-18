package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.Army.{Aggression, DefendZones}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly14Nexus

class PvT13Nexus extends Mode {
  
  description.set("PvT 13 Nexus")
  
  override val activationCriteria: Plan = new Employing(PvTEarly14Nexus)
  override val completionCriteria: Plan = new UnitsAtLeast(8, Protoss.Dragoon)
  
  children.set(Vector(
    new Aggression(0.8),
    new BuildOrder(ProtossBuilds.Opening13Nexus_Long: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Protoss.Observer, 1),
    new TrainContinuously(Protoss.Dragoon),
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(8, Protoss.Gateway)),
    new ScoutAt(14),
    new DefendZones
  ))
}

