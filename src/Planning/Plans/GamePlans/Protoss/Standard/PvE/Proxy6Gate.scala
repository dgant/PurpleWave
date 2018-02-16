package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Protoss.Situational.{DefendAgainstProxy, PlaceGatewaysProxied}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss

class Proxy6Gate extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticAggressive
  
  private class BeforeProxy extends Parallel(
    new RequireEssentials,
    new Trigger(new UnitsAtLeast(1, Protoss.Gateway), initialBefore = new PlaceGatewaysProxied(1, () => proxyZone)),
    new Build(
      RequestAtLeast(6, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon)),
    new If(new UnitsAtLeast(1, Protoss.Pylon), new Build(RequestAtLeast(1, Protoss.Gateway))))
  
  private class AfterProxy extends Parallel(
    new TrainContinuously(Protoss.Zealot, 2),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new Build(RequestAtLeast(5, Protoss.Gateway)),
    new Scout)
  
  children.set(Vector(
    new Aggression(99.0),
    new Do(() =>  With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(1, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy),
    new DefendAgainstProxy,
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}