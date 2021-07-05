package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, ConsiderAttacking}
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyDarkTemplarLikely, SafeToMoveOut}
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.MatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPRobo, PvPRobo1012}

class PvPRobo extends GameplanTemplate {

  override val activationCriteria: Predicate = Employing(PvPRobo)
  override val completionCriteria: Predicate = Latch(BasesAtLeast(2))

  val oneGateCoreLogic = new PvP1GateCoreLogic(allowZealotBeforeCore = true)

  class ShuttleFirst extends ConcludeWhen(
    UnitsAtLeast(1, Protoss.RoboticsSupportBay),
    And(
      UnitsAtMost(0, Protoss.RoboticsSupportBay),
      Or(
        Check(() => With.strategy.isRamped),
        Not(new GetObservers)),
      Not(new oneGateCoreLogic.GateTechGateGate),
      Or(
        EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo),
        EnemyBasesAtLeast(2))))

  class GetObservers extends Or(
    new EnemyDarkTemplarLikely,
    EnemiesAtLeast(1, Protoss.CitadelOfAdun),
    And(
      EnemyRecentStrategy(With.fingerprints.dtRush),
      Not(EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon))),
    Not(EnemyStrategy(With.fingerprints.dragoonRange)))

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 7.0)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.ShieldBattery))

  override def workerPlan: Plan = new PumpWorkers

  // TODO: Replace with (or merge into) PvPSafeToMoveOut?
  override def attackPlan: Plan = new If(
    And(
      EnemyStrategy(With.fingerprints.twoGate),
      UnitsAtLeast(1, Protoss.Dragoon, complete = true),
      Or(
        UpgradeComplete(Protoss.DragoonRange),
        Not(EnemyHasUpgrade(Protoss.DragoonRange)),
        SafeToMoveOut())),
    new Attack,
    new If(
      And(
        // No point attacking with a couple of Zealots if they have any defense whatsoever
        Or(
          Or(Employing(PvPRobo1012)),
          UnitsAtLeast(1, Protoss.Dragoon, complete = true),
          UnitsAtLeast(1, Protoss.Reaver, complete = true),
          EnemiesAtMost(0, Protoss.PhotonCannon, complete = true),
          EnemiesAtMost(0, MatchWarriors)),
        Or(
          Not(EnemyHasShown(Protoss.DarkTemplar)),
          UnitsAtLeast(2, Protoss.Observer, complete = true)),
        Or(
          And(EnemyStrategy(With.fingerprints.dtRush), UnitsAtLeast(2, Protoss.Observer, complete = true)),
          EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.gasSteal, With.fingerprints.cannonRush, With.fingerprints.earlyForge),
          EnemyBasesAtLeast(2))),
        new ConsiderAttacking))

  override def emergencyPlans: Seq[Plan] = oneGateCoreLogic.emergencyPlans
  override def scoutPlan: Plan = new If(EnemiesAtMost(0, Protoss.Dragoon), super.scoutPlan)

  override def aggressionPlan: Plan = new Trigger(
    And(new ReadyToExpand, BasesAtMost(1)),
    new Aggression(3.0),
    new If(
      Check(() => With.strategy.isInverted), new Aggression(1.5),
      new If(
        Check(() => With.strategy.isFlat), new Aggression(1.2))))

  override def buildOrderPlan: Plan = new oneGateCoreLogic.BuildOrderPlan

  private class TrainRoboUnits extends Parallel(
    new Trigger(
      UnitsAtLeast(1, Protoss.RoboticsFacility),
      new Parallel(
        new If(
          new GetObservers,
          new Parallel(
            new If(
              EnemyStrategy(With.fingerprints.dtRush),
              new Pump(Protoss.Observer, 2)),
          new BuildOrder(Get(Protoss.Observer)))),
        new If(new ShuttleFirst, new BuildOrder(Get(Protoss.Shuttle))),
        new Trigger(
          UnitsAtLeast(2, Protoss.Reaver),
          new If(
            And(
              EnemyStrategy(With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon),
              UnitsAtMost(3, Protoss.Reaver)),
            new Pump(Protoss.Reaver),
            new PumpShuttleAndReavers),
          new Pump(Protoss.Reaver, 2)))))

  private class TrainGatewayUnits extends Parallel(
    new BuildOrder(Get(Protoss.Dragoon)),
    new Pump(Protoss.Dragoon),
    new If(
      UnitsAtLeast(3, Protoss.Gateway),
      new Pump(Protoss.Zealot)))

  class EnemyLowUnitCount extends Or(
    EnemyBasesAtLeast(2),
    EnemyStrategy(
      With.fingerprints.robo,
      With.fingerprints.dtRush,
      With.fingerprints.forgeFe))

  class ReadyToExpand extends And(
    UnitsAtLeast(2, Protoss.Gateway),
    Or(Not(EnemyStrategy(With.fingerprints.dtRush)), UnitsAtLeast(1, Protoss.Observer, complete = true)),
    Or(Not(EnemyStrategy(With.fingerprints.dtRush)), And(UnitsAtLeast(1, Protoss.Observer), EnemiesAtMost(0, Protoss.DarkTemplar))),
    Or(
      And(new SafeToMoveOut, EnemyStrategy(With.fingerprints.dtRush, With.fingerprints.twoGate)),
      And(new SafeToMoveOut, UnitsAtLeast(1, Protoss.Reaver, complete = true), new EnemyLowUnitCount),
      UnitsAtLeast(2, Protoss.Reaver, complete = true)))

  override def buildPlans: Seq[Plan] = Seq(
    new oneGateCoreLogic.WriteStatuses,
    new If(new GetObservers, new WriteStatus("Obs"), new WriteStatus("NoObs")),
    new If(new GasCapsUntouched, new CapGasAt(350)),
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
          UnitsAtLeast(1, Protoss.Observer),
          new Build(Get(Protoss.RoboticsSupportBay)))),
      new Parallel(
        new CancelIncomplete(Protoss.Observatory),
        new CancelIncomplete(Protoss.Observer),
        new Build(Get(Protoss.RoboticsSupportBay)))),

    new If(
      new ReadyToExpand,
      new Parallel(new WriteStatus("ReadyToExpand"),
        new If(
          Check(() => With.geography.ourNatural.units.exists(u => u.isEnemy && u.canAttack)),
          new Attack,
          new RequireMiningBases(2)))),

    new TrainGatewayUnits,
    new If(EnemyStrategy(With.fingerprints.dtRush), new Build(Get(Protoss.ObserverSpeed))),
    new Build(Get(3, Protoss.Gateway)),
    new PumpWorkers(oversaturate = true),
  )
}
