package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{And, If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientPylons, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.FindEnemyBase
import ProxyBwapi.Races.Protoss

class Proxy2GateAtNatural extends Parallel {
  
  private def proxyZone: Option[Zone] = {
    With.geography.bases.find(base => base.isNaturalOf.isDefined && ! base.owner.isUs).map(_.zone)
  }
  
  children.set(Vector(
    new Plan { override def onUpdate(): Unit = { With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue }},
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone),
        new Blueprint(this, building = Some(Protoss.Gateway), zone = proxyZone))
    },
    new Build(
      RequestAtLeast(1, Protoss.Nexus),
      RequestAtLeast(9, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon)),
    
    // Crappy haxx to make this all work
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
      new Build(RequestAtLeast(1, Protoss.Gateway))),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Gateway), complete = false),
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    new Trigger(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Gateway), complete = false),
      initialAfter = new Parallel(
        new RequireSufficientPylons,
        new TrainContinuously(Protoss.Zealot),
        new TrainContinuously(Protoss.Probe),
        new TrainContinuously(Protoss.Gateway))),
    new If(
      new And (
        new UnitsAtLeast(7, UnitMatchType(Protoss.Probe), complete = false),
        new UnitsAtMost(1, UnitMatchType(Protoss.Pylon), complete = false)),
      new FindEnemyBase),
    
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}