package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, Hunt}
import Planning.Plans.Basic.{Do, NoPlan, Write}
import Planning.Plans.Compound.{Or, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlaceGatewaysProxied
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyIsTerran, EnemyStrategy}
import Planning.UnitCounters.UnitCountOne
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Protoss.{PvPProxy2Gate, PvRProxy2Gate, PvTProxy2Gate, PvZProxy2Gate}

class PvEProxy2Gate extends GameplanTemplate {

  override val activationCriteria = new Employing(PvRProxy2Gate, PvTProxy2Gate, PvPProxy2Gate, PvZProxy2Gate)
  override val completionCriteria = new Latch(new BasesAtLeast(2))
  override def scoutPlan = new If(
    new Not(new FoundEnemyBase),
    new ScoutOn(Protoss.Gateway, quantity = 2))

  override def aggressionPlan: Plan = new Aggression(1.2)
  override def workerPlan: Plan = NoPlan()

  override def priorityAttackPlan: Plan = new Parallel(
    new If(
      new EnemyStrategy(With.fingerprints.cannonRush),
      new Hunt(Protoss.Zealot, Protoss.Probe, UnitCountOne)),
    new Attack)
  
  private class BeforeProxy extends Parallel(
    new PlaceGatewaysProxied(2, () => ProxyPlanner.proxyMiddle),
    new If(
      new EnemyIsTerran,
      new BuildOrder(Get(8, Protoss.Probe)),
      new BuildOrder(Get(9, Protoss.Probe))),
    new BuildOrder(
      Get(Protoss.Pylon),
      Get(9, Protoss.Probe)),
    new If(new UnitsAtLeast(1, Protoss.Pylon),    new Build(Get(1, Protoss.Gateway))),
    new If(new UnitsAtLeast(1, Protoss.Gateway),  new Build(Get(2, Protoss.Gateway))))
  
  private class MustTech extends Or(
    new UnitsAtLeast(15, Protoss.Probe),
    new EnemiesAtLeast(1, Protoss.Forge),
    new EnemiesAtLeast(1, Protoss.PhotonCannon),
    new EnemyHasShownCloakedThreat,
    new EnemyHasShown(Terran.Vulture),
    new EnemyHasShown(Protoss.Dragoon),
    new EnemiesAtLeast(1, Zerg.Spire, complete = true),
    new EnemiesAtLeast(1, Terran.Factory, complete = true),
    new EnemyWalledIn)

  private class AfterProxy extends Parallel(
    new If(
      new FrameAtLeast(GameTime(3, 30)())),
      new If(
        new EnemyStrategy(With.fingerprints.oneGateCore),
        new If(
          new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
          new Aggression(2.0),
          new Aggression(1.9))),

    new RequireSufficientSupply,
    new BuildOrder(
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(Protoss.Zealot),
      Get(11, Protoss.Probe),
      Get(2, Protoss.Zealot),
      Get(2, Protoss.Pylon),
      Get(4, Protoss.Zealot),
      Get(12, Protoss.Probe)),

    new Pump(Protoss.Observer, 2),
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Pump(Protoss.RoboticsFacility, 1),
        new Pump(Protoss.Observatory, 1))),

    new UpgradeContinuously(Protoss.DragoonRange),
    new If(
      new Not(new UpgradeStarted(Protoss.DragoonRange)),
      new CapGasAt(200)),

    new If(
      new And(
        new UnitsAtLeast(8, Protoss.Dragoon),
        new SafeAtHome),
      new RequireBases(2)),

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
        new MineralsAtLeast(250)),
      new Build(Get(4, Protoss.Gateway))),

    new Pump(Protoss.Zealot),
  )

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush
  )
  
  override def buildPlans = Vector(
    new Write(With.blackboard.pushKiters, true),
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy)
  )
}