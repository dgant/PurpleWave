package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateRange

class PvT1GateRange extends Mode {
  
  description.set("PvT 13 Nexus")
  
  override val activationCriteria: Plan = new Employing(PvTEarly1GateRange)
  override val completionCriteria: Plan = new UnitsAtLeast(2, Protoss.Nexus, complete = true)
  
  children.set(Vector(
    new BuildOrder(ProtossBuilds.Opening1GateRangeExpand: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new TrainContinuously(Protoss.Dragoon),
    new BuildOrder(RequestAtLeast(6, Protoss.Gateway)),
    new ScoutAt(14),
    new DefendZones,
    new ConsiderAttacking
  ))
}

