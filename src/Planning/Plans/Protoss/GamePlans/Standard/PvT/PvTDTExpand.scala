package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{Attack, DefendZones}
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarlyDTExpand

class PvTDTExpand extends Mode {
  
  description.set("PvT DT Expand")
  
  override val activationCriteria: Plan = new Employing(PvTEarlyDTExpand)
  override val completionCriteria: Plan = new UnitsAtLeast(2, Protoss.Nexus, complete = true)
  
  children.set(Vector(
    new BuildOrder(ProtossBuilds.OpeningDTExpand: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new If(
      new UnitsAtMost(2, Protoss.DarkTemplar),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new TrainContinuously(Protoss.Dragoon)),
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(4, Protoss.Gateway)),
    new ScoutAt(14),
    new PvTIdeas.AttackWithDarkTemplar,
    new DefendZones,
    new Trigger(
      new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
      initialAfter = new Attack)
  ))
}

