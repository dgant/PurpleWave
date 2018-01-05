package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarlyDTExpand

class PvTDTExpand extends GameplanModeTemplate {
  
  override val activationCriteria     = new Employing(PvTEarlyDTExpand)
  override val completionCriteria     = new UnitsAtLeast(2, Protoss.Nexus, complete = true)
  override val buildOrder             = ProtossBuilds.OpeningDTExpand
  override val defaultWorkerPlan      = new TrainWorkersContinuously(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan      = new Trigger(
    new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true),
    initialAfter = super.defaultAttackPlan)
  
  override val buildPlans = Vector(
    new If(
      new UnitsAtMost(2, Protoss.DarkTemplar),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new TrainContinuously(Protoss.Dragoon)),
    new Build(
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(4, Protoss.Gateway)))
}

