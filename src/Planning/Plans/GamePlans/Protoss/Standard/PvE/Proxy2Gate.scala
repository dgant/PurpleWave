package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound.{Or, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlaceGatewaysProxied
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, RequireSufficientSupply, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyIsProtoss, EnemyStrategy}
import Planning.Predicates.Milestones._
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Protoss.{PvPOpenProxy2Gate, PvTProxy2Gate, PvZProxy2Gate}
import Strategery.Strategies.Protoss.PvR.PvROpenProxy2Gate

class Proxy2Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvROpenProxy2Gate, PvTProxy2Gate, PvPOpenProxy2Gate, PvZProxy2Gate)
  override val completionCriteria = new Latch(new BasesAtLeast(2))
  override def defaultScoutPlan   = new If(new UnitsAtLeast(2, Protoss.Gateway), new Scout)
  override def defaultWorkerPlan: Plan = NoPlan()
  override def defaultAttackPlan: Plan = new Attack
  
  private class BeforeProxy extends Parallel(
    new PlaceGatewaysProxied(2, () => ProxyPlanner.proxyAutomaticSneaky),
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(1, Protoss.Pylon),
      Get(9, Protoss.Probe)),
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
            new If(
              new And(
                new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.Dragoon.buildFrames),
                new GasAtLeast(50)),
              new Pump(Protoss.Dragoon)))))),
    new If(
      new Or(
        new UnitsAtLeast(15, Protoss.Probe),
        new MineralsAtLeast(300)),
      new Parallel(
        new Build(Get(3, Protoss.Gateway)),
        new RequireMiningBases(2)))
  )
  
  override def buildPlans = Vector(
    new Do(() =>  With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy)
  )
}