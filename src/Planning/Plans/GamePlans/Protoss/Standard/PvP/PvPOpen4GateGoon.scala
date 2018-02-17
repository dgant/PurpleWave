package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen4GateGoon

class PvPOpen4GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria : Plan = new Employing(PvPOpen4GateGoon)
  override val completionCriteria : Plan = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan = new PvPIdeas.AttackSafely
  override val scoutAt            : Int  = 14
  override val defaultWorkerPlan  : Plan = NoPlan()
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.Opening_4GateDragoon
  
  override val buildPlans = Vector(
    new FlipIf(
      new UnitsAtLeast(20, Protoss.Dragoon),
      new RequireMiningBases(2),
      new TrainContinuously(Protoss.Dragoon)))
}
