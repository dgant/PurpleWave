package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Scouting.{CampExpansions, FindExpansions}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Maps.{StarCraftMap, Transistor}
import Strategery.Strategies.Zerg.ZvPTwoHatchMuta

class ZvPTwoHatchMuta extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(ZvPTwoHatchMuta)
  
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
  
  class MapIs(map: StarCraftMap) extends Plan {
    override def isComplete: Boolean = map.matches
  }
  
  private def sunkenProfile = if (Transistor.matches)
    new PlacementProfile(
      "TransistorSunkens",
      avoidDistanceFromBase = 1.0,
      preferDistanceFromEnemy = 0.25)
  else
    PlacementProfiles.wallCannon
  
  
  override def priorityAttackPlan: Plan = new If(
    new UnitsAtLeast(12, UnitMatchWarriors),
    new CampExpansions)
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new ShouldDoSpeedlingAllIn,
      new EnemyBasesAtLeast(2)),
    new Attack)
  
  override def defaultScoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new Trigger(
      new UnitsAtLeast(2, Zerg.Overlord),
      new ScoutSafelyWithDrone))
  
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
      new Parallel(
        new BuildOrder(
          RequestAtLeast(1, Zerg.Extractor),
          RequestAtLeast(17, Zerg.Drone),
          RequestAtLeast(1, Zerg.Lair)),
        new If(
          new UnitsAtLeast(16, Zerg.Drone),
          new Build(RequestAtLeast(2, Zerg.Extractor))),
        new BuildOrder(
          RequestAtLeast(21, Zerg.Drone),
          RequestAtLeast(3, Zerg.Overlord),
          RequestAtLeast(3, Zerg.Hatchery),
          RequestAtLeast(1, Zerg.Spire))),
      new If(
        new EnemyStrategy(With.intelligence.fingerprints.gatewayFe),
        new BuildOrder(RequestAtLeast(8, Zerg.Zergling)),
        new Parallel(
          new Trigger(
            new UnitsAtLeast(2, UnitMatchOr(Zerg.SunkenColony, Zerg.CreepColony)),
            initialBefore = new BuildSunkensAtNatural(2, sunkenProfile)),
          new BuildOrder(RequestAtLeast(10, Zerg.Zergling))))))
    
  lazy val ZealotOrDragoon = UnitMatchOr(Protoss.Zealot, Protoss.Dragoon)
  
  class ReactiveSunkensVsZealots extends If(
    new And(
      new UnitsAtLeast(12, Zerg.Drone),
      new UnitsAtMost(0, Zerg.Spire, complete = true)),
    new If(
      new EnemyUnitsAtLeast(18, ZealotOrDragoon),
      new BuildSunkensAtNatural(7, sunkenProfile),
      new If(
        new EnemyUnitsAtLeast(14, ZealotOrDragoon),
        new BuildSunkensAtNatural(6, sunkenProfile),
        new If(
          new EnemyUnitsAtLeast(11, ZealotOrDragoon),
          new BuildSunkensAtNatural(5, sunkenProfile),
          new If(
            new EnemyUnitsAtLeast(8, ZealotOrDragoon),
            new BuildSunkensAtNatural(4, sunkenProfile),
            new If(
              new EnemyUnitsAtLeast(5, ZealotOrDragoon),
              new BuildSunkensAtNatural(3, sunkenProfile),
              new If(
                new EnemyUnitsAtLeast(3, ZealotOrDragoon),
                new BuildSunkensAtNatural(2, sunkenProfile))))))))
  
  class ReactiveZerglingsVsZealots extends If(
    new UnitsAtMost(0, Zerg.Spire, complete = true),
    new TrainMatchingRatio(Zerg.Zergling, 0, 10, Seq(
      MatchingRatio(Protoss.Zealot, 4.0))))
  
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
    new WatchIslandsOnThirdWorld,
    new TakeThirdWorldIslandsAfter(2),
    new If(
      new IfOnMiningBases(5),
      new Aggression(1.3),
      new If(
        new IfOnMiningBases(4),
        new Aggression(1.15),
        new If(
          new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
          new Aggression(1.0),
          new Aggression(0.8)))),
    
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
      new UnitsAtLeast(1, Zerg.Hive),
      new UpgradeContinuously(Zerg.ZerglingAttackSpeed)),
  
    new If(
      new And(
        new MiningBasesAtLeast(4),
        new UnitsAtLeast(32, Zerg.Drone)),
      new Build(
        RequestAtLeast(1, Zerg.QueensNest),
        RequestAtLeast(1, Zerg.Hive),
        RequestAtLeast(2, Zerg.EvolutionChamber))),
  
    new If(
      new And(
        new MiningBasesAtLeast(3),
        new UnitsAtLeast(24, Zerg.Drone)),
      new Parallel(
        new UpgradeContinuously(Zerg.GroundArmor),
        new If(
          new Or(
            new UnitsAtLeast(2, Zerg.EvolutionChamber),
            new UpgradeComplete(Zerg.GroundArmor, 3),
            new And(
              new UpgradeComplete(Zerg.GroundArmor, 2),
              new UnitsAtMost(0, Zerg.Hive))),
          new UpgradeContinuously(Zerg.GroundMeleeDamage)),
        new Build(RequestAtLeast(1, Zerg.EvolutionChamber)))),
    
    new If(
      new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
      new Parallel(
        new If(
          new And(
            new Or(
              new Not(new UpgradeComplete(Zerg.AirDamage, 3)),
              new And(
                new Not(new UpgradeComplete(Zerg.AirDamage, 2)),
                new UnitsAtMost(0, Zerg.Hive, complete = true))),
            new Or(
              new EnemyHasShown(Protoss.Corsair),
              new EnemyHasShown(Protoss.Stargate),
              new UpgradeComplete(Zerg.AirDamage, 3),
              new And(
                new UpgradeComplete(Zerg.AirDamage, 3),
                new UnitsAtMost(0, Zerg.Hive, complete = true)))),
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
        new NeedExpansion(20),
        new ReadyToExpand),
      new RequireMiningBases(3)),
    new If(
      new And(
        new NeedExpansion(26),
        new ReadyToExpand),
      new RequireBases(4)),
    new If(
      new And(
        new NeedExpansion(32),
        new ReadyToExpand),
      new RequireBases(5)),
    new If(
      new And(
        new NeedExpansion(40),
        new ReadyToExpand),
      new RequireBases(6)),
    new If(
      new And(
        new NeedExpansion(50),
        new ReadyToExpand),
      new RequireBases(7)),
    new If(
      new ProceedWithDrones,
      new TrainContinuously(Zerg.Drone, 16)),
    new TrainContinuously(Zerg.Zergling, 4),
    new If(
      new ProceedWithDrones,
      new TrainContinuously(Zerg.Drone, 24)),
    new IfOnMiningBases(3, new TrainContinuously(Zerg.Drone, 24)),
    new IfOnMiningBases(4, new TrainContinuously(Zerg.Drone, 32)),
    new IfOnMiningBases(5, new TrainContinuously(Zerg.Drone, 40)),
    new IfOnMiningBases(6, new TrainContinuously(Zerg.Drone, 48)),
        
    new TrainContinuously(Zerg.Zergling)
  )
}
