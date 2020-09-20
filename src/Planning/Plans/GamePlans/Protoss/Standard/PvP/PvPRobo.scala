package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, ConsiderAttacking, DefendNatural, EjectScout}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.Predicates.Compound.{And, Latch, Not, ConcludeWhen}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo

class PvPRobo extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvPRobo)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  class ShuttleFirst extends ConcludeWhen(
    new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
    new And(
      new UnitsAtMost(0, Protoss.RoboticsSupportBay),
      new Not(new EnemyStrategy(With.fingerprints.dtRush)),
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.twoGate),
        new EnemyBasesAtLeast(2),
        new EnemyStrategy(With.fingerprints.robo),
        new EnemyRecentStrategy(With.fingerprints.fourGateGoon))))

  class GetObservers extends Or(
    new EnemyDarkTemplarLikely,
    new EnemiesAtLeast(1, Protoss.CitadelOfAdun),
    new And(
      new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.fourGateGoon)),
      new Or(
        new Not(new EnemyStrategy(With.fingerprints.dragoonRange)),
        new EnemyRecentStrategy(With.fingerprints.dtRush))))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 7.0)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.ShieldBattery),
  )

  // TODO: Replace with (or merge into) PvPSafeToMoveOut?
  // TODO: Handle 4-Gate Zealot
  override def attackPlan: Plan = new If(
    new And(
      // No point attacking with Zealots if they have any defense whatsoever
      new Or(
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new UnitsAtLeast(1, Protoss.Reaver, complete = true),
        new EnemiesAtMost(0, Protoss.PhotonCannon, complete = true),
        new EnemiesAtMost(1, UnitMatchWarriors)),
      new Or(
        new Not(new EnemyHasShown(Protoss.DarkTemplar)),
        new UnitsAtLeast(2, Protoss.Observer, complete = true)),
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.gasSteal, With.fingerprints.cannonRush, With.fingerprints.earlyForge),
        new And(new PvP1GateCoreIdeas.GateGate, new EnemyStrategy(With.fingerprints.oneGateCore), new Not(new EnemyStrategy(With.fingerprints.fourGateGoon))),
        new EnemyBasesAtLeast(2),
        new And(new EnemyStrategy(With.fingerprints.dtRush), new UnitsAtLeast(2, Protoss.Observer, complete = true)),
        new And(new EnemyStrategy(With.fingerprints.twoGate),
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
              new UnitsAtLeast(2, Protoss.Reaver, complete = true)))))),
      new ConsiderAttacking)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush,
    new If(
      new EnemyDarkTemplarLikely,
      new If(
        new Latch(new UnitsAtMost(0, Protoss.CyberneticsCore)),
        new PvPIdeas.ReactToDarkTemplarEmergencies,
        new Parallel(
          new If(new UnitsAtMost(0, Protoss.Observatory), new CancelIncomplete(Protoss.RoboticsSupportBay)),
          new If(new UnitsAtMost(0, Protoss.Observer), new CancelIncomplete(Protoss.Shuttle, Protoss.Reaver)),
          new BuildOrder(
            Get(Protoss.RoboticsFacility),
            Get(Protoss.Observatory),
            Get(2, Protoss.Observer))))))

  override def scoutExposPlan: Plan = NoPlan()
  override def defendEntrance: Plan = new Trigger(new ReadyToExpand, new DefendNatural, super.defendEntrance)

  override def aggressionPlan: Plan = new Trigger(
    new And(
      new ReadyToExpand,
      new EnemyStrategy(With.fingerprints.fourGateGoon)),
    new Aggression(1.5)
  )

  override def buildOrderPlan: Plan = new PvP1GateCoreIdeas.BuildOrderPlan

  private class TrainArmyHighPri extends Parallel(
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
          new If(
            new And(
              new EnemyStrategy(With.fingerprints.fourGateGoon),
              new UnitsAtMost(3, Protoss.Reaver)),
            new Pump(Protoss.Reaver),
            new PumpShuttleAndReavers),
          new Pump(Protoss.Reaver, 2)))))

  private class TrainArmyLowPri extends Parallel(
    // Make sure we don't accidentally start an extra Zealot due to tight gas
    new BuildOrder(Get(Protoss.Dragoon)),
    new Pump(Protoss.Dragoon),
    new If(
      new UnitsAtLeast(3, Protoss.Gateway),
      new Pump(Protoss.Zealot, maximumConcurrently = 1)))

  class Expand extends RequireMiningBases(2)

  class EnemyLowUnitCount extends Or(
    new EnemyBasesAtLeast(2),
    new EnemyStrategy(
      With.fingerprints.robo,
      With.fingerprints.dtRush,
      With.fingerprints.earlyForge))

  class ReadyToExpand extends And(
    new UnitsAtLeast(2, Protoss.Gateway),
    new Or(
      new And(new UnitsAtLeast(1, Protoss.Reaver), new EnemyLowUnitCount),
      new And(new UnitsAtLeast(2, Protoss.Reaver, complete = true), new And(new SafeAtHome, new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)))),
      new And(
        new Latch(new UnitsAtLeast(3, UnitMatchOr(Protoss.Shuttle, Protoss.Reaver), complete = true)),
        new UnitsAtLeast(2, UnitMatchOr(Protoss.Shuttle, Protoss.Reaver), complete = true))))

  override def buildPlans: Seq[Plan] = Seq(

    new EjectScout,

    new If(new GasCapsUntouched, new CapGasAt(350)),

    new Trigger(
      new UnitsAtLeast(1, Protoss.Dragoon),
      new If(
        new PvP1GateCoreIdeas.GateGate),
        new Build(Get(2, Protoss.Gateway))),

    // Keep pumping robo units even if expanding
    new TrainArmyHighPri,

    new If(new ReadyToExpand, new Expand),

    // This flip is important to ensure that Gate Gate Robo gets its tech in timely fashion
    new FlipIf(
      new And(
        new UnitsAtLeast(2, Protoss.Dragoon),
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway))),
      new TrainArmyLowPri,
      new Parallel(
        new If(new PvP1GateCoreIdeas.GateGate, new Build(Get(2, Protoss.Gateway))),
        new Build(Get(Protoss.RoboticsFacility)),
        new Build(Get(2, Protoss.Gateway)),
        new If(
          new GetObservers,
          new Build(Get(Protoss.Observatory)),
          new Parallel(
            new CancelIncomplete(Protoss.Observatory),
            new Build(Get(Protoss.RoboticsSupportBay)))),
        new If(new UnitsAtLeast(1, Protoss.Observatory), new Build(Get(Protoss.RoboticsSupportBay))))),

    new If(new EnemyStrategy(With.fingerprints.dtRush), new Build(Get(Protoss.ObserverSpeed))),
    new If(new Not(new EnemyLowUnitCount), new Build(Get(3, Protoss.Gateway))),
    new PumpWorkers(oversaturate = true),

    new Expand,

    new Build(Get(5, Protoss.Gateway), Get(2, Protoss.Assimilator))
  )
}
