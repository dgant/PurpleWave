package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{ConsiderAttacking, EjectScout}
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{RequireBases, RequireMiningBases}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeAtHome}
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

class PvPRobo extends GameplanTemplate {

  class ScoutOnGateway extends StartPositionsAtLeast(3)

  class ZealotBeforeCore extends Or(
    new StartPositionsAtMost(2),
    new EnemyRecentStrategy(With.fingerprints.mannerPylon, With.fingerprints.proxyGateway, With.fingerprints.cannonRush, With.fingerprints.workerRush, With.fingerprints.nexusFirst))

  class ZealotAfterCore extends Or(new Not(new EnemyStrategy(With.fingerprints.oneGateCore)))

  class GateGateRobo extends Or(
    new Check(() => With.strategy.isFlat || With.strategy.isInverted))

  class ShuttleFirst extends Or(
    new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.twoGate),
    new EnemyBasesAtLeast(2))

  class GetObservers extends Or(
    new EnemyStrategy(With.fingerprints.dtRush),
    new EnemiesAtLeast(1, Protoss.CitadelOfAdun),
    new And(
      new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.fourGateGoon)),
      new EnemyBasesAtMost(1),
      new Or(
        new Not(new EnemyStrategy(With.fingerprints.dragoonRange)),
        new EnemyRecentStrategy(With.fingerprints.dtRush))))

  override val activationCriteria: Predicate = new Employing(PvPRobo)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))
  override def scoutPlan: Plan = new If(new ScoutOnGateway, new ScoutOn(Protoss.Gateway), new ScoutOn(Protoss.CyberneticsCore))

  // TODO: Building placement

  // TODO: Gate Gate wants to attack
  // TODO: Replace with (or merge into) PvPSafeToMoveOut?
  // TODO: Handle 4-Gate Zealot
  override def attackPlan: Plan = new If(
    new Or(
      new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.gasSteal, With.fingerprints.cannonRush, With.fingerprints.earlyForge),
      // TODO: One-Gate FE
      new And(
        new EnemyStrategy(With.fingerprints.dtRush),
        new UnitsAtLeast(2, Protoss.Observer, complete = true)),
       new And(
        new EnemyStrategy(With.fingerprints.twoGate),
        new Or(
          new EnemyHasShown(Protoss.Gateway), // Don't abandon base vs. proxies
          new UnitsAtLeast(7, UnitMatchWarriors)),
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new Or(
          new UpgradeComplete(Protoss.DragoonRange),
          new Not(new EnemyHasUpgrade(Protoss.DragoonRange)))),
      new And(
        new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
        new Latch(
          new And(
            new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
            new UnitsAtLeast(2, Protoss.Reaver, complete = true))))),
    new ConsiderAttacking)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)

  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe)),
    new If(
      new ZealotBeforeCore,
      new Parallel(
        new BuildOrder(
          Get(Protoss.Zealot),
          Get(14, Protoss.Probe),
          Get(2, Protoss.Pylon),
          Get(15, Protoss.Probe),
          Get(Protoss.CyberneticsCore),
          Get(16, Protoss.Probe)),
        new If(
          new ZealotAfterCore,
          new BuildOrder(Get(2, Protoss.Zealot)))),
      new BuildOrder(Get(Protoss.CyberneticsCore))),
    new If(new ZealotAfterCore, new BuildOrder(Get(2, Protoss.Zealot))))

  private class TrainArmy extends Parallel(
    new UpgradeContinuously(Protoss.DragoonRange),
    new Trigger(
      new UnitsAtLeast(1, Protoss.RoboticsFacility),
      new Parallel(
        new If(
          new GetObservers,
          new Parallel(
            new If(
              new EnemyStrategy(With.fingerprints.dtRush),
              new Pump(Protoss.Observer, 2)),
          new BuildOrder(Get(Protoss.Observer)))),
        new If(new ShuttleFirst, new BuildOrder(Get(Protoss.Shuttle))),
        new Trigger(
          new UnitsAtLeast(2, Protoss.Reaver),
          new PumpShuttleAndReavers,
          new Pump(Protoss.Reaver, 2)))),
    new Pump(Protoss.Dragoon),
    new If(
      new UnitsAtLeast(3, Protoss.Gateway),
      new Pump(Protoss.Zealot, maximumConcurrently = 1)))

  override def buildPlans: Seq[Plan] = Seq(

    new EjectScout,

    // TODO: Cap/uncap gas
    new If(
      new GasCapsUntouched,
      new CapGasAt(300)),

    // TODO: Be useful for PvPVsForge:
      // TODO: React properly vs. cannon rush

    // Expand
    new Trigger(
      new Or(
        new And(
          new UnitsAtLeast(2, Protoss.Reaver, complete = true),
          new Or(
            new SafeAtHome,
            new EnemyBasesAtLeast(2))),
        new UnitsAtLeast(4, Protoss.Reaver, complete = true)),
      new RequireBases(2)),

    // This flip is important to ensure that Gate Gate Robo gets its tech in timely fashion
    new FlipIf(
      new And(
        new UnitsAtLeast(4, UnitMatchWarriors),
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway))),
      new TrainArmy,
      new Parallel(
        new If(new GateGateRobo, new Build(Get(2, Protoss.Gateway))),
        new Build(Get(Protoss.RoboticsFacility)),
        new If(new GetObservers, new Build(Get(Protoss.Observatory))),
        new Build(Get(Protoss.RoboticsSupportBay)))),

    new If(new EnemyStrategy(With.fingerprints.fourGateGoon), new Build(Get(3, Protoss.Gateway))),

    new RequireMiningBases(2),

    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator),
      Get(Protoss.ShuttleSpeed),
      Get(6, Protoss.Gateway))
  )
}
