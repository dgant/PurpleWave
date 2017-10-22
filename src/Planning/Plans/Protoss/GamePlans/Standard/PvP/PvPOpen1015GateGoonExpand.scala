package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1015GateGoonExpand

class PvPOpen1015GateGoonExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1015GateGoonExpand)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely
  override val scoutAt            : Int       = 14
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  override def buildPlans = Vector(
    new FlipIf(
      new And(
        new UnitsAtLeast(5, Protoss.Dragoon),
        new Or(
          new UnitsAtLeast(8, Protoss.Dragoon),
          new SafeAtHome)),
      new Parallel(
        new TrainContinuously(Protoss.Dragoon),
        new Build(RequestAtLeast(4, Protoss.Gateway)),
      new RequireMiningBases(2)))
  )
}
