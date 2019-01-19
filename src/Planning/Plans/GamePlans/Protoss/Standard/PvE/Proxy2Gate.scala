package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound.{Or, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlaceGatewaysProxied
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyIsTerran, EnemyStrategy}
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Protoss.{PvPProxy2Gate, PvROpenProxy2Gate, PvTProxy2Gate, PvZProxy2Gate}

class Proxy2Gate extends GameplanTemplate {
  
  override val activationCriteria = new Employing(PvROpenProxy2Gate, PvTProxy2Gate, PvPProxy2Gate, PvZProxy2Gate)
  override val completionCriteria = new Latch(new BasesAtLeast(2))
  override def scoutPlan   = new If(new UnitsAtLeast(2, Protoss.Gateway), new Scout)
  override def workerPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Attack
  
  private class BeforeProxy extends Parallel(
    new PlaceGatewaysProxied(2, () => ProxyPlanner.proxyMiddle),
    new If(
      new EnemyIsTerran,
      new BuildOrder(Get(8, Protoss.Probe), Get(1, Protoss.Pylon)),
      new BuildOrder(Get(9, Protoss.Probe), Get(1, Protoss.Pylon))),
    new If(new UnitsAtLeast(1, Protoss.Pylon),    new Build(Get(1, Protoss.Gateway))),
    new If(new UnitsAtLeast(1, Protoss.Gateway),  new Build(Get(2, Protoss.Gateway))))
  
  private class MustTech extends Or(
    new UnitsAtLeast(15, Protoss.Probe),
    new EnemyHasShownCloakedThreat,
    new EnemyHasShown(Terran.Vulture),
    new EnemyHasShown(Protoss.Dragoon),
    new EnemiesAtLeast(1, Zerg.Spire, complete = true),
    new EnemiesAtLeast(1, Terran.Factory, complete = true),
    new EnemyWalledIn)
  
  private class AfterProxy extends Parallel(
    new If(
      new EnemyStrategy(With.fingerprints.oneGateCore),
      new If(
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new Aggression(5.0),
        new Aggression(1.9))),

    new RequireSufficientSupply,
    new BuildOrder(Get(1, Protoss.Gateway), Get(2, Protoss.Zealot)),

    new Pump(Protoss.Observer, 2),
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Pump(Protoss.RoboticsFacility, 1),
        new Pump(Protoss.Observatory, 1))),

    new UpgradeContinuously(Protoss.DragoonRange),
    new If(
      new Not(new UpgradeStarted(Protoss.DragoonRange)),
      new CapGasAt(150)),

    new FlipIf(
      new MustTech,
      new Pump(Protoss.Zealot, 10),
      new Parallel(
        new PumpWorkers,
        new If(
          new MustTech,
          new Parallel(
            new BuildGasPumps(1),
            new Build(Get(1, Protoss.CyberneticsCore)),
            new Pump(Protoss.Dragoon))))),
    new If(
      new Or(
        new UnitsAtLeast(15, Protoss.Probe),
        new MineralsAtLeast(300)),
      new Parallel(
        new Build(Get(3, Protoss.Gateway)),
        new RequireMiningBases(2))),

    new Pump(Protoss.Zealot)
  )
  
  override def buildPlans = Vector(
    new Do(() =>  With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy)
  )
}