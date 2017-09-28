package Planning.Plans.Protoss.GamePlans.Specialty

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Protoss.Situational.{DefendAgainstProxy, PlaceGatewaysProxied}
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss

class Proxy2Gate extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  
  private class BeforeProxy extends Parallel(
    new RequireEssentials,
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway), initialBefore = new PlaceGatewaysProxied(2, () => proxyZone)),
    new Build(
      RequestAtLeast(9, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon)),
    // Build proxies with only one Probe
    new If(new UnitsAtLeast(1, Protoss.Pylon),    new Build(RequestAtLeast(1, Protoss.Gateway))),
    new If(new UnitsAtLeast(1, Protoss.Gateway),  new Build(RequestAtLeast(2, Protoss.Gateway))))
  
  private class AfterProxy extends Parallel(
    new RequireSufficientSupply,
    new BuildOrder(
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Zealot)),
    new TrainContinuously(Protoss.Observer, 2),
    new If(
      new And(
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeTime(1)),
        new Check(() => With.self.gas >= 50)),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot, 8)),
    new TrainWorkersContinuously,
    new Trigger(
      new UnitsAtLeast(15, Protoss.Probe),
      initialAfter = new Parallel(
      new BuildGasPumps,
      new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
      new If(
        new EnemyHasShownCloakedThreat,
        new Parallel(
          new BuildGasPumps,
        new Build(
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)))),
      new Build(RequestUpgrade(Protoss.DragoonRange)),
      new Build(RequestAtLeast(3, Protoss.Gateway)),
      new RequireMiningBases(2),
      new Build(RequestAtLeast(7, Protoss.Gateway)),
      new RequireMiningBases(3),
      new Build(RequestAtLeast(12, Protoss.Gateway)))),
    new Scout)
  
  children.set(Vector(
    new Aggression(1.5),
    new Do(() =>  With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy),
    new DefendAgainstProxy,
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}