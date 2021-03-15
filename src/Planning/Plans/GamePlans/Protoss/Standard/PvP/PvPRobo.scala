package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, ConsiderAttacking}
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{Or, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound._
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely, SafeToMoveOut}
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.MatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPRobo, PvPRobo1012}

class PvPRobo extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvPRobo)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = true)

  class ShuttleFirst extends ConcludeWhen(
    new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
    new And(
      new UnitsAtMost(0, Protoss.RoboticsSupportBay ),
      new Or(
        new Check(() => With.strategy.isRamped),
        new Not(new GetObservers)),
      new Not(new oneGateCoreLogic.GateTechGateGate),
      new Or(
        new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo),
        new EnemyBasesAtLeast(2))))

  class GetObservers extends Or(
    new EnemyDarkTemplarLikely,
    new EnemiesAtLeast(1, Protoss.CitadelOfAdun),
    new And(
      new EnemyRecentStrategy(With.fingerprints.dtRush),
      new Not(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))),
    new Not(new EnemyStrategy(With.fingerprints.dragoonRange, With.fingerprints.dragoonRange)))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 7.0)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.ShieldBattery))

  override def workerPlan: Plan = new PumpWorkers

  // TODO: Replace with (or merge into) PvPSafeToMoveOut?
  override def attackPlan: Plan = new If(
    new And(
      // No point attacking with a couple of Zealots if they have any defense whatsoever
      new Or(
        new Or(new Employing(PvPRobo1012)),
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new UnitsAtLeast(1, Protoss.Reaver, complete = true),
        new EnemiesAtMost(0, Protoss.PhotonCannon, complete = true),
        new EnemiesAtMost(0, MatchWarriors)),
      new Or(
        new Not(new EnemyHasShown(Protoss.DarkTemplar)),
        new UnitsAtLeast(2, Protoss.Observer, complete = true)),
      new Or(
        new And(new EnemyStrategy(With.fingerprints.dtRush), new UnitsAtLeast(2, Protoss.Observer, complete = true)),
        new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.gasSteal, With.fingerprints.cannonRush, With.fingerprints.earlyForge),
        new EnemyBasesAtLeast(2),
        new And(
          new EnemyStrategy(With.fingerprints.twoGate),
          new Not(new EnemyStrategy(With.fingerprints.proxyGateway)),
          new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
          new Or(
            new UpgradeComplete(Protoss.DragoonRange),
            new Not(new EnemyHasUpgrade(Protoss.DragoonRange)))))),
      new ConsiderAttacking)

  override def emergencyPlans: Seq[Plan] = oneGateCoreLogic.emergencyPlans
  override def scoutPlan: Plan = new If(new EnemiesAtMost(0, Protoss.Dragoon), super.scoutPlan)

  override def aggressionPlan: Plan = new Trigger(
    new And(new ReadyToExpand, new BasesAtMost(1)),
    new Aggression(3.0),
    new If(
      new Check(() => With.strategy.isInverted), new Aggression(1.5),
      new If(
        new Check(() => With.strategy.isFlat), new Aggression(1.2))))

  override def buildOrderPlan: Plan = new oneGateCoreLogic.BuildOrderPlan

  private class TrainRoboUnits extends Parallel(
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
              new EnemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon),
              new UnitsAtMost(3, Protoss.Reaver)),
            new Pump(Protoss.Reaver),
            new PumpShuttleAndReavers),
          new Pump(Protoss.Reaver, 2)))))

  private class TrainGatewayUnits extends Parallel(
    new BuildOrder(Get(Protoss.Dragoon)),
    new Pump(Protoss.Dragoon),
    new If(
      new UnitsAtLeast(3, Protoss.Gateway),
      new Pump(Protoss.Zealot)))

  class EnemyLowUnitCount extends Or(
    new EnemyBasesAtLeast(2),
    new EnemyStrategy(
      With.fingerprints.robo,
      With.fingerprints.dtRush,
      With.fingerprints.forgeFe))

  class ReadyToExpand extends And(
    new UnitsAtLeast(2, Protoss.Gateway),
    new Or(new Not(new EnemyStrategy(With.fingerprints.dtRush)), new UnitsAtLeast(1, Protoss.Observer, complete = true)),
    new Or(new Not(new EnemyStrategy(With.fingerprints.dtRush)), new And(new UnitsAtLeast(1, Protoss.Observer), new EnemiesAtMost(0, Protoss.DarkTemplar))),
    new Or(
      new And(new SafeToMoveOut, new EnemyStrategy(With.fingerprints.dtRush)),
      new And(new SafeToMoveOut, new UnitsAtLeast(1, Protoss.Reaver, complete = true), new EnemyLowUnitCount),
      new UnitsAtLeast(2, Protoss.Reaver, complete = true)))

  override def buildPlans: Seq[Plan] = Seq(
    new oneGateCoreLogic.WriteStatuses,
    new If(new GetObservers, new WriteStatus("Obs"), new WriteStatus("NoObs")),
    new If(
      new GasCapsUntouched,
      new Parallel(
        new CapGasAt(350),
        new If(          new oneGateCoreLogic.GateGate))),
    new FlipIf(
      new oneGateCoreLogic.GateGate,
      new Parallel(
        new BuildOrder(
          Get(Protoss.Dragoon),
          Get(Protoss.DragoonRange),
          Get(3, Protoss.Pylon),
          Get(Protoss.RoboticsFacility),
          Get(2, Protoss.Dragoon)),
        new If(
          new oneGateCoreLogic.GateTechGateGate,
          new Build(Get(3, Protoss.Gateway)))),
      new BuildOrder(
        Get(2, Protoss.Gateway),
        Get(Protoss.Dragoon),
        Get(Protoss.DragoonRange),
        Get(3, Protoss.Pylon),
        Get(3, Protoss.Dragoon))),

    new TrainRoboUnits,

    new If(
      new GetObservers,
      new Parallel(
        new BuildOrder(Get(Protoss.Observatory)),
        new Trigger(
          new UnitsAtLeast(1, Protoss.Observer),
          new Build(Get(Protoss.RoboticsSupportBay)))),
      new Parallel(
        new CancelIncomplete(Protoss.Observatory),
        new CancelIncomplete(Protoss.Observer),
        new Build(Get(Protoss.RoboticsSupportBay)))),

    new If(new ReadyToExpand, new Parallel(new WriteStatus("ReadyToExpand"), new RequireMiningBases(2))),
    new TrainGatewayUnits,
    new If(new EnemyStrategy(With.fingerprints.dtRush), new Build(Get(Protoss.ObserverSpeed))),
    new Build(Get(3, Protoss.Gateway)),
    new PumpWorkers(oversaturate = true),
    new If(
      new And(
        new UnitsAtLeast(3, Protoss.Gateway, complete = true),
        new MineralsAtLeast(450)),
      new Build(Get(4, Protoss.Gateway))),
  )
}
