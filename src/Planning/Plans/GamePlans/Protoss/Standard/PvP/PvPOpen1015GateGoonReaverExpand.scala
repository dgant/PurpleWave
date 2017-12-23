package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1015GateReaverExpand

class PvPOpen1015GateGoonReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1015GateReaverExpand)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely
  override val scoutAt            : Int       = 14
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  override def buildPlans = Vector(
    new FlipIf(
      new UnitsAtLeast(5, UnitMatchWarriors),
      new Parallel(
        new TrainContinuously(Protoss.Reaver, 2),
        new TrainContinuously(Protoss.Dragoon)),
      new Parallel(
        new BuildOrder(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay),
          RequestAtLeast(1, Protoss.Reaver)),
        new RequireMiningBases(2)
      )))
}
