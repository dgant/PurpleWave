package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchOr
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.{CampExpansions, FindExpansions, Scout}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZergVsProtoss

class ZergVsProtoss extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(ZergVsProtoss)
  
  class ShouldDoSpeedlingAllIn extends EnemyStrategy(
    With.intelligence.fingerprints.cannonRush,
    With.intelligence.fingerprints.proxyGateway)
  
  class ProceedWithTech extends Or(
      new Latch(new UnitsAtLeast(16, Zerg.Drone)),
      new Latch(new UnitsAtLeast(8, Zerg.Zergling)),
      new Latch(new UnitsAtLeast(2, Zerg.SunkenColony)),
      new Not(new EnemyStrategy(
        With.intelligence.fingerprints.twoGate,
        With.intelligence.fingerprints.proxyGateway)))
  
  class ProceedWithDrones extends Or(
    new ProceedWithTech,
    new Latch(new UnitsAtLeast(1, Zerg.Zergling)))
    
  class SafeForOverlords extends EnemyUnitsAtMost(0, UnitMatchOr(
    Protoss.Dragoon,
    Protoss.Corsair,
    Protoss.Stargate,
    Protoss.CyberneticsCore),
    complete = true)
  
  override def priorityAttackPlan: Plan = new CampExpansions
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      new ShouldDoSpeedlingAllIn,
      new EnemyBasesAtLeast(2)),
    new Attack)
  
  override def defaultScoutPlan: Plan = new If(
    new Parallel(
      new If(
        new SafeForOverlords,
        new Scout { scouts.get.unitMatcher.set(Zerg.Overlord) }),
      new Trigger(
        new UnitsAtLeast(2, Zerg.Overlord),
        new If(
          new UnitsAtMost(0, UnitMatchOr(Protoss.Zealot, Protoss.PhotonCannon), complete = true),
          new Scout))))
  
  override def defaultScoutExposPlan: Plan = new If(
    new UnitsAtLeast(8, Zerg.Mutalisk),
    new FindExpansions)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(12, Zerg.Drone),
      RequestAtLeast(2, Zerg.Hatchery),
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(14, Zerg.Drone)),
    new If(
      new EnemyStrategy(
        With.intelligence.fingerprints.forgeFe,
        With.intelligence.fingerprints.nexusFirst),
      new BuildOrder(
        RequestAtLeast(1, Zerg.Extractor),
        RequestAtLeast(17, Zerg.Drone),
        RequestAtLeast(1, Zerg.Lair),
        RequestAtLeast(2, Zerg.Extractor),
        RequestAtLeast(21, Zerg.Drone),
        RequestAtLeast(3, Zerg.Overlord),
        RequestAtLeast(3, Zerg.Hatchery),
        RequestAtLeast(1, Zerg.Spire)),
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.gatewayFe),
        new BuildOrder(RequestAtLeast(8, Zerg.Zergling)),
        new Parallel(
          new Trigger(
            new UnitsAtLeast(2, UnitMatchOr(Zerg.SunkenColony, Zerg.CreepColony)),
            initialBefore = new BuildSunkensAtNatural(2)),
          new BuildOrder(RequestAtLeast(10, Zerg.Zergling))))))
    
  lazy val ZealotOrDragoon = UnitMatchOr(Protoss.Zealot, Protoss.Dragoon)
  
  class ReactiveSunkensVsZealots extends If(
    new And(
      new UnitsAtLeast(12, Zerg.Drone),
      new UnitsAtMost(0, Zerg.Spire, complete = true)),
    new If(
      new EnemyUnitsAtLeast(18, ZealotOrDragoon),
      new BuildSunkensAtNatural(7),
      new If(
        new EnemyUnitsAtLeast(14, ZealotOrDragoon),
        new BuildSunkensAtNatural(6),
        new If(
          new EnemyUnitsAtLeast(11, ZealotOrDragoon),
          new BuildSunkensAtNatural(5),
          new If(
            new EnemyUnitsAtLeast(8, ZealotOrDragoon),
            new BuildSunkensAtNatural(4),
            new If(
              new EnemyUnitsAtLeast(5, ZealotOrDragoon),
              new BuildSunkensAtNatural(3),
              new If(
                new EnemyUnitsAtLeast(2, ZealotOrDragoon),
                new BuildSunkensAtNatural(2))))))))
  
  class ReactiveZerglingsVsZealots extends If(
    new UnitsAtMost(0, Zerg.Spire, complete = true),
    new TrainMatchingRatio(Zerg.Zergling, 0, 10, Seq(
      MatchingRatio(Protoss.Zealot, 4.0))))
  
  class DoSpeedlingAllIn extends Parallel(
    new Aggression(1.2),
    new BuildOrder(RequestAtLeast(10, Zerg.Zergling)),
    new If(
      new Or(
        new GasAtLeast(100),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1))),
      new Do(() => { With.blackboard.gasLimitFloor = 0; With.blackboard.gasLimitCeiling = 0 })),
    new FlipIf(
      new SafeAtHome,
      new TrainContinuously(Zerg.Zergling),
      new TrainContinuously(Zerg.Drone, 9)),
    new Build(
      RequestAtLeast(1, Zerg.Extractor),
      RequestUpgrade(Zerg.ZerglingSpeed)),
    new RequireMiningBases(3),
    new If(
      new MineralsAtLeast(400),
      new RequireMiningBases(4)))
  
  class NeedExpansion(droneCount: Int) extends Or(
    new UnitsAtLeast(droneCount, Zerg.Drone),
    new MineralsAtLeast(500))
  
  class ReadyToExpand extends Or(
    new UnitsAtLeast(1, Zerg.Spire, complete = true),
    new MineralsAtLeast(300))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new TrainContinuously(Zerg.SunkenColony),
    new ReactiveSunkensVsZealots)
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      new Aggression(1.0),
      new Aggression(0.8)),
    
    new Do(() => With.blackboard.gasLimitFloor = 0),
    new If(
      new Not(new ProceedWithTech)),
      new Do(() => With.blackboard.gasLimitCeiling = 0),
      new If(
        new UnitsAtMost(1, Zerg.Spire),
        new Do(() => With.blackboard.gasLimitCeiling = With.self.minerals + 50),
        new Do(() => With.blackboard.gasLimitCeiling = With.self.minerals + 200)),
    new If(
      new EnemyHasShown(Protoss.Corsair),
      new TrainContinuously(Zerg.Scourge, 2)),
    new TrainMatchingRatio(Zerg.Scourge, 0, 10, Seq(MatchingRatio(UnitMatchOr(Protoss.Corsair, Protoss.Stargate), 2.0))),
    new If(
      new And(
        new MiningBasesAtLeast(3),
        new UnitsAtLeast(6, Zerg.Mutalisk),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.Zergling.buildFrames)),
      new TrainMatchingRatio(Zerg.Zergling, 0, 18, Seq(MatchingRatio(Protoss.Dragoon, 3.0)))),
    new If(
      new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
      new Parallel(
        new If(
          new Or(
            new EnemyHasShown(Protoss.Corsair),
            new EnemyHasShown(Protoss.Stargate)),
          new UpgradeContinuously(Zerg.AirArmor),
          new If(
            new UnitsAtLeast(6, Zerg.Mutalisk),
            new UpgradeContinuously(Zerg.AirDamage))),
        new TrainContinuously(Zerg.Mutalisk))),
    new If(
      new ShouldDoSpeedlingAllIn,
      new DoSpeedlingAllIn),
    new ReactiveZerglingsVsZealots,
    new If(
      new ProceedWithDrones,
      new TrainContinuously(Zerg.Drone, 12),
      new TrainContinuously(Zerg.Drone, 9)),
    new If(
      new ProceedWithTech,
      new Build(
        RequestAtLeast(1, Zerg.Extractor),
        RequestAtLeast(1, Zerg.Lair),
        RequestAtLeast(1, Zerg.Spire),
        RequestUpgrade(Zerg.ZerglingSpeed))),
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new Parallel(
        new If(
          new UnitsAtLeast(18, Zerg.Drone),
          new BuildGasPumps(2)),
        new If(
          new UnitsAtLeast(26, Zerg.Drone),
          new BuildGasPumps(3)),
        new If(
          new UnitsAtLeast(34, Zerg.Drone),
          new BuildGasPumps(5)))),
    new If(
      new And(
        new NeedExpansion(16),
        new ReadyToExpand),
      new RequireMiningBases(3)),
    new If(
      new And(
        new NeedExpansion(20),
        new ReadyToExpand),
      new RequireMiningBases(4)),
    new If(
      new And(
        new NeedExpansion(25),
        new ReadyToExpand),
      new RequireMiningBases(5)),
    new If(
      new And(
        new NeedExpansion(30),
        new ReadyToExpand),
      new RequireMiningBases(6)),
    new If(
      new ProceedWithDrones,
      new TrainContinuously(Zerg.Drone, 16)),
    new TrainContinuously(Zerg.Zergling, 4),
    new If(
      new ProceedWithDrones,
      new TrainContinuously(Zerg.Drone, 24)),
    new IfOnMiningBases(3, new TrainContinuously(Zerg.Drone, 27)),
    new IfOnMiningBases(4, new TrainContinuously(Zerg.Drone, 32)),
    new TrainContinuously(Zerg.Zergling)
  )
}
