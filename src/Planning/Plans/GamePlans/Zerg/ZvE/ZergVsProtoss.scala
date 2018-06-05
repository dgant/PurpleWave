package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchOr
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.SafeAtHome
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Zerg}

class ZergVsProtoss extends GameplanModeTemplate {
  
  class DetectedStrategy extends EnemyStrategy(
    With.intelligence.fingerprints.cannonRush,
    With.intelligence.fingerprints.proxyGateway,
    With.intelligence.fingerprints.twoGate,
    With.intelligence.fingerprints.oneGateCore,
    With.intelligence.fingerprints.gatewayFe,
    With.intelligence.fingerprints.forgeFe,
    With.intelligence.fingerprints.nexusFirst)
  
  override def defaultScoutPlan: Plan = new Parallel(
    new If(
      new EnemyUnitsAtMost(0, UnitMatchOr(
        Protoss.Dragoon,
        Protoss.Corsair,
        Protoss.Stargate,
        Protoss.CyberneticsCore),
        complete = true),
      new Scout(20) { scouts.get.unitMatcher.set(Zerg.Overlord) }),
    new Trigger(
      new UnitsAtLeast(12, Zerg.Drone),
      new If(
        new Not(new DetectedStrategy)),
        new Scout))
  
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
    
  class ReactiveSunkensVsZealots extends If(
    new And(
      new UnitsAtLeast(12, Zerg.Drone),
      new UnitsAtMost(0, Zerg.Spire, complete = true)),
    new If(
      new EnemyUnitsAtLeast(16, Protoss.Zealot),
      new BuildSunkensAtNatural(7),
      new If(
        new EnemyUnitsAtLeast(12, Protoss.Zealot),
        new BuildSunkensAtNatural(5),
        new If(
          new EnemyUnitsAtLeast(8, Protoss.Zealot),
          new BuildSunkensAtNatural(4),
          new If(
            new EnemyUnitsAtLeast(5, Protoss.Zealot),
            new BuildSunkensAtNatural(3),
            new If(
              new EnemyUnitsAtLeast(2, Protoss.Zealot),
              new BuildSunkensAtNatural(2)))))))
  
  class ReactiveZerglingsVsZealots extends If(
    new UnitsAtMost(0, Zerg.Spire, complete = true),
    new TrainMatchingRatio(Zerg.Zergling, 0, 12, Seq(
      MatchingRatio(Protoss.Zealot, 3.0),
      MatchingRatio(Protoss.Gateway, 2.0))))
  
  class DoSpeedlingAllIn extends Parallel(
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
  
  class ReactToProxyGates extends If(
    new EnemyStrategy(With.intelligence.fingerprints.proxyGateway),
    new DoSpeedlingAllIn)
    
  class ReactToNexusFirst extends If(
    new EnemyStrategy(With.intelligence.fingerprints.nexusFirst),
    new DoSpeedlingAllIn)
  
  class ReactToCannonRush extends If(
    new EnemyStrategy(With.intelligence.fingerprints.cannonRush),
    new DoSpeedlingAllIn)
  
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new TrainContinuously(Zerg.SunkenColony),
    new ReactiveSunkensVsZealots)
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.gasLimitFloor = 0),
    new If(
      new And(
        new EnemyStrategy(With.intelligence.fingerprints.twoGate),
        new UnitsAtMost(16, Zerg.Drone)),
      new Do(() => With.blackboard.gasLimitCeiling = 0),
      new If(
        new UnitsAtMost(1, Zerg.Spire),
        new Do(() => With.blackboard.gasLimitCeiling = With.self.minerals + 50),
        new Do(() => With.blackboard.gasLimitCeiling = With.self.minerals + 200))),
    new TrainMatchingRatio(Zerg.Scourge, 0, 8, Seq(MatchingRatio(UnitMatchOr(Protoss.Corsair, Protoss.Stargate), 2.0))),
    new If(
      new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
      new Parallel(
        new If(
          new UnitsAtLeast(1, UnitMatchOr(Protoss.Corsair, Protoss.Stargate)),
          new UpgradeContinuously(Zerg.AirArmor),
          new If(
            new UnitsAtLeast(6, Zerg.Mutalisk),
            new UpgradeContinuously(Zerg.AirDamage))),
        new TrainContinuously(Zerg.Mutalisk))),
   
    new ReactToProxyGates,
    new ReactToCannonRush,
    new ReactToNexusFirst,
    new TrainContinuously(Zerg.Drone, 12),
    new ReactiveZerglingsVsZealots,
    new Build(
      RequestAtLeast(1, Zerg.Extractor),
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(1, Zerg.Spire)),
    new UpgradeContinuously(Zerg.ZerglingSpeed),
    new If(
      new UnitsAtLeast(16, Zerg.Drone),
      new BuildGasPumps),
    new If(
      new UnitsAtLeast(20, Zerg.Drone),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(25, Zerg.Drone),
      new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(30, Zerg.Drone),
      new RequireMiningBases(5)),
    new TrainContinuously(Zerg.Drone, 24),
    new IfOnMiningBases(3, new TrainContinuously(Zerg.Drone, 27)),
    new IfOnMiningBases(4, new TrainContinuously(Zerg.Drone, 32)),
    new TrainContinuously(Zerg.Zergling)
  )
}
