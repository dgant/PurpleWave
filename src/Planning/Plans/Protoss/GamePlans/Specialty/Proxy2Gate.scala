package Planning.Plans.Protoss.GamePlans.Specialty

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Protoss.Situational.{DefendAgainstProxy, PlaceTwoGatewaysProxied}
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class Proxy2Gate extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  private class ProxyPlan extends Parallel(
    new TrainContinuously(Protoss.Zealot),
    new TrainWorkersContinuously,
    new Trigger(
      new Check(() => With.frame > 24 * 60 * 3),
      initialAfter = new TrainContinuously(Protoss.Gateway, 5)))
  
  private class TransitionPlan extends Parallel(
    new BuildGasPumps,
    new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory))),
    new Build(RequestUpgrade(Protoss.DragoonRange)),
    new RequireSufficientSupply,
    new TrainContinuously(Protoss.Observer, 2),
    new TrainWorkersContinuously,
    new If(
      new And(
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeTime(1)),
        new Check(() => With.self.gas >= 50 )),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
   
    new Build(RequestAtLeast(4, Protoss.Gateway)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(12, Protoss.Gateway)))
  
  private class ReactivePlan extends Trigger(
    new Or(
      new EnemyHasShownCloakedThreat,
      new EnemyUnitsAtLeast(1, Terran.Vulture),
      new EnemyUnitsAtLeast(1, Zerg.Spire),
      new EnemyUnitsAtLeast(1, Zerg.Mutalisk),
      new EnemyUnitsAtLeast(1, Protoss.Carrier)),
    initialBefore = new ProxyPlan,
    initialAfter  = new TransitionPlan)
  
  children.set(Vector(
    new RequireEssentials,
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway), initialBefore = new PlaceTwoGatewaysProxied(() => proxyZone)),
    new Build(
      RequestAtLeast(9, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon)),
    
    // Build proxies with only one Probe
    new If(new UnitsAtLeast(1, Protoss.Pylon),    new Build(RequestAtLeast(1, Protoss.Gateway))),
    new If(new UnitsAtLeast(1, Protoss.Gateway),  new Build(RequestAtLeast(2, Protoss.Gateway))),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway), initialAfter = new ReactivePlan),
    
    new Scout,
    new DefendAgainstProxy,
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}