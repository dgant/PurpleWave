package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtMost, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1015GateDTs

class PvPOpen1015GateGoonDTs extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1015GateDTs)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely
  override val scoutAt            : Int       = 14
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  private class EnemyNoMobileDetection extends And(
    new EnemyUnitsAtMost(0, Protoss.Observer),
    new EnemyUnitsAtMost(0, Protoss.Observatory))
    
  override def buildPlans = Vector(
    new If(
      new EnemyNoMobileDetection,
      new TrainContinuously(Protoss.DarkTemplar, 2)),
    new FlipIf(
      new UnitsAtLeast(5, Protoss.Dragoon),
      new TrainContinuously(Protoss.Dragoon),
      new Parallel(
        new If(
          new EnemyNoMobileDetection,
          new Build(
            RequestAtLeast(1, Protoss.CitadelOfAdun),
            RequestAtLeast(1, Protoss.TemplarArchives))),
        new RequireMiningBases(2),
        new Build(RequestAtLeast(4, Protoss.Gateway)))
    ))
}
