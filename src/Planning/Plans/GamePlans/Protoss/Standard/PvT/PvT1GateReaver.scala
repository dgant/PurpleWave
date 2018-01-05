package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchOr
import Planning.Plans.Army.DropAttack
import Planning.Plans.Compound.{If, NoPlan}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateReaver

class PvT1GateReaver extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1GateReaver)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val superSaturate      = true
  override def defaultAttackPlan  = NoPlan()
  override def scoutAt            = 8
  
  override def defaultDropPlan = new If (
    new UnitsAtLeast(1, Protoss.Reaver),
    new DropAttack { paratrooperMatcher.set(UnitMatchOr(Protoss.Zealot, Protoss.Reaver)) })

  // Hide the Robotics Facility
  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon),             placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.RoboticsFacility),  placement = Some(PlacementProfiles.tech)))
  
  override val buildOrder = Vector(
    //CoreZ, Scout @ Pylon -- from Antiga replay
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.RoboticsFacility))
  
  override def buildPlans = Vector(
    new BuildOrder(RequestAtLeast(1, Protoss.Shuttle)),
    new PvTIdeas.TrainArmy,
    new BuildOrder(
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestAtLeast(1, Protoss.Reaver)),
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(3, Protoss.Gateway)),
    new BuildGasPumps)
}