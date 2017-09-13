package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.Army.DefendZones
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly14Nexus

class PvT13Nexus extends Mode {
  
  description.set("PvT 13 Nexus")
  
  override val activationCriteria: Plan = new Employing(PvTEarly14Nexus)
  override val completionCriteria: Plan = new UnitsAtLeast(6, Protoss.Dragoon)
  
  children.set(Vector(
    new BuildOrder(ProtossBuilds.Opening13Nexus_Long: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Protoss.Dragoon),
    new BuildOrder(RequestAtLeast(8, Protoss.Gateway)),
    new ScoutAt(14),
    new DefendZones
  ))
}

