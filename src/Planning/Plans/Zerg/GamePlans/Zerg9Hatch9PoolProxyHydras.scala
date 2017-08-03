package Planning.Plans.Zerg.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{AddSupplyWhenSupplyBlocked, Gather, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Milestones.UnitsAtMost
import Planning.ProxyPlanner
import ProxyBwapi.Races.Zerg

class Zerg9Hatch9PoolProxyHydras extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticHatchery
  
  val buildUpToZerglings = Vector(
    RequestAtLeast(8,   Zerg.Drone),
    RequestAtLeast(1,   Zerg.Overlord),
    RequestAtLeast(13,  Zerg.Drone),
    RequestAtLeast(2,   Zerg.Hatchery),
    RequestAtLeast(1,   Zerg.SpawningPool))
  
  val buildUpToHydras = Vector(
    RequestAtLeast(1,   Zerg.Extractor),
    RequestAtLeast(2,   Zerg.Overlord),
    RequestAtLeast(1,   Zerg.HydraliskDen),
    RequestAtLeast(15,  Zerg.Drone),
    RequestAtLeast(3,   Zerg.Overlord))
  
  children.set(Vector(
    new ProposePlacement { override lazy val blueprints = Vector(new Blueprint(this, building = Some(Zerg.Hatchery), preferZone = proxyZone)) },
    new Aggression(2.0),
    new Build(buildUpToZerglings: _*),
    new If(
      new UnitsAtMost(0, UnitMatchType(Zerg.HydraliskDen), complete = true),
      new TrainContinuously(Zerg.Zergling, 8),
      new Parallel(
        new AddSupplyWhenSupplyBlocked,
        new TrainContinuously(Zerg.Hydralisk)
      )),
    new Build(buildUpToHydras: _*),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
