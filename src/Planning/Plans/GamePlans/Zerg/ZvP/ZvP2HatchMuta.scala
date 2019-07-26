package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{ScoutSafelyWithDrone, ScoutSafelyWithOverlord}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Scouting.{CampExpansions, FindExpansions}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Zerg.ZvP2HatchMuta
import Strategery.{StarCraftMap, Transistor}

class ZvP2HatchMuta extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(ZvP2HatchMuta)
  
  class ProceedWithTech extends Or(
      new Latch(new UnitsAtLeast(14, Zerg.Drone)),
      new Latch(new UnitsAtLeast(8, Zerg.Zergling, countEggs = true)),
      new Latch(new UnitsAtLeast(2, Zerg.SunkenColony)),
      new Not(new EnemyStrategy(
        With.fingerprints.twoGate,
        With.fingerprints.proxyGateway)))
  
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
    PlacementProfiles.defensive
  
  override def priorityAttackPlan: Plan = new If(
    new UnitsAtLeast(12, UnitMatchWarriors),
    new CampExpansions(Zerg.Zergling))
  
  override def attackPlan: Plan = new If(
    new Or(
      new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new ShouldDoSpeedlingAllIn,
      new EnemyBasesAtLeast(2)),
    new Attack)
  
  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new Trigger(
      new UnitsAtLeast(2, Zerg.Overlord),
      new ScoutSafelyWithDrone))
  
  override def scoutExposPlan: Plan = new If(
    new UnitsAtLeast(8, Zerg.Mutalisk),
    new FindExpansions)
  
  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(12, Zerg.Drone),
      Get(2, Zerg.Hatchery),
      Get(Zerg.SpawningPool),
      Get(Zerg.Extractor),
      Get(14, Zerg.Drone)),
    new If(
      new EnemyStrategy(
        With.fingerprints.forgeFe,
        With.fingerprints.nexusFirst),
      new Parallel(
        new BuildOrder(
          Get(17, Zerg.Drone),
          Get(Zerg.Lair)),
        new If(
          new UnitsAtLeast(16, Zerg.Drone),
          new Build(Get(2, Zerg.Extractor))),
        new BuildOrder(
          Get(21, Zerg.Drone),
          Get(3, Zerg.Overlord),
          Get(3, Zerg.Hatchery),
          Get(Zerg.Spire)))),
    new If(
      new EnemyStrategy(With.fingerprints.gatewayFe),
      new BuildOrder(Get(8, Zerg.Zergling)),
      new Parallel(
        new Trigger(
          new UnitsAtLeast(2, UnitMatchOr(Zerg.SunkenColony, Zerg.CreepColony)),
          initialBefore = new BuildSunkensAtNatural(2, sunkenProfile)),
        new BuildOrder(Get(10, Zerg.Zergling)))))
    
  lazy val ZealotOrDragoon = UnitMatchOr(Protoss.Zealot, Protoss.Dragoon)
  
  class ReactiveSunkensVsZealots extends If(
    new And(
      new UnitsAtLeast(12, Zerg.Drone),
      new UnitsAtMost(0, Zerg.Spire, complete = true)),
    new If(
      new EnemiesAtLeast(17, ZealotOrDragoon),
      new BuildSunkensAtNatural(7, sunkenProfile),
      new If(
        new EnemiesAtLeast(13, ZealotOrDragoon),
        new BuildSunkensAtNatural(6, sunkenProfile),
        new If(
          new EnemiesAtLeast(10, ZealotOrDragoon),
          new BuildSunkensAtNatural(5, sunkenProfile),
          new If(
            new EnemiesAtLeast(7, ZealotOrDragoon),
            new BuildSunkensAtNatural(4, sunkenProfile),
            new If(
              new EnemiesAtLeast(5, ZealotOrDragoon),
              new BuildSunkensAtNatural(3, sunkenProfile),
              new If(
                new EnemiesAtLeast(3, ZealotOrDragoon),
                new BuildSunkensAtNatural(2, sunkenProfile))))))))
  
  class ReactiveZerglings extends If(
    new UnitsAtMost(0, Zerg.Spire, complete = true),
    new PumpRatio(Zerg.Zergling, 0, 10, Seq(
      Friendly(UnitMatchAnd(Zerg.SunkenColony, UnitMatchComplete), -6.0),
      Enemy(Terran.Marine, 1.5),
      Enemy(Terran.Firebat, 3.0),
      Enemy(Terran.Vulture, 2.0),
      Enemy(Protoss.Zealot, 4.0),
      Enemy(Zerg.Zergling, 1.75))))
  
  class NeedExpansion(droneCount: Int) extends Or(
    new UnitsAtLeast(droneCount, Zerg.Drone),
    new MineralsAtLeast(500))
  
  class ReadyToExpand extends Or(
    new UnitsAtLeast(1, Zerg.Spire, complete = true),
    new MineralsAtLeast(300))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new Pump(Zerg.SunkenColony),
    new PumpRatio(Zerg.Drone, 9, 18, Seq(Friendly(UnitMatchAnd(Zerg.SunkenColony, UnitMatchComplete)))),
    new ReactiveSunkensVsZealots,
    new ZergReactionVsWorkerRush
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new If(
      new MiningBasesAtLeast(5),
      new Aggression(1.3),
      new If(
        new MiningBasesAtLeast(4),
        new Aggression(1.15),
        new If(
          new UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
          new Aggression(1.0),
          new Aggression(0.8)))),
  
    new CapGasAt(0),
    new If(
      new Not(new ProceedWithTech)),
      new CapGasAt(0),
      new If(
        new UnitsAtMost(1, Zerg.Spire),
        new CapGasAtRatioToMinerals(1.0, 50),
        new CapGasAtRatioToMinerals(1.0, 200)),
    new If(
      new EnemyHasShown(Protoss.Corsair),
      new Pump(Zerg.Scourge, 2)),
    new PumpRatio(Zerg.Scourge, 0, 10,
      Seq(
        Enemy(UnitMatchOr(Terran.Valkyrie, Terran.Wraith, Protoss.Corsair, Protoss.Stargate, Zerg.Mutalisk), 2.0),
        Enemy(UnitMatchOr(Zerg.Scourge), 1.0))),
    new If(
      new And(
        new MiningBasesAtLeast(3),
        new UnitsAtLeast(6, Zerg.Mutalisk),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.Zergling.buildFrames)),
      new PumpRatio(Zerg.Zergling, 0, 18, Seq(
        Enemy(Terran.Marine, 1.5),
        Enemy(Terran.Goliath, 4.0),
        Enemy(Protoss.Dragoon, 3.0),
        Enemy(Zerg.Hydralisk, 3.0)))),
  
    new If(
      new UnitsAtLeast(1, Zerg.Hive),
      new UpgradeContinuously(Zerg.ZerglingAttackSpeed)),
  
    new If(
      new And(
        new MiningBasesAtLeast(4),
        new UnitsAtLeast(32, Zerg.Drone)),
      new Build(
        Get(Zerg.QueensNest),
        Get(Zerg.Hive),
        Get(2, Zerg.EvolutionChamber))),
  
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
        new Build(Get(1, Zerg.EvolutionChamber)))),

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
              new EnemyHasShown(Terran.Marine),
              new EnemyHasShown(Terran.Goliath),
              new EnemyHasShown(Terran.Valkyrie),
              new EnemyHasShown(Protoss.Corsair),
              new EnemyHasShown(Protoss.Stargate),
              new EnemyHasShown(Zerg.Mutalisk),
              new UpgradeComplete(Zerg.AirDamage, 3),
              new And(
                new UpgradeComplete(Zerg.AirDamage, 3),
                new UnitsAtMost(0, Zerg.Hive, complete = true)))),
          new UpgradeContinuously(Zerg.AirArmor),
          new If(
            new UnitsAtLeast(6, Zerg.Mutalisk),
            new UpgradeContinuously(Zerg.AirDamage))),
        new Pump(Zerg.Mutalisk))),
    
    new If(
      new ShouldDoSpeedlingAllIn,
      new DoSpeedlingAllIn),
    
    new ReactiveZerglings,
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new BuildOrder(
        Get(5, Zerg.Overlord),
        Get(8, Zerg.Mutalisk))), // make sure we have enough when mutas pop

    new Pump(Zerg.Drone, 9),
    new If(new ProceedWithDrones, new Pump(Zerg.Drone, 12)),
    new If(
      new ProceedWithTech,
      new Build(
        Get(Zerg.Lair),
        Get(Zerg.Spire),
        Get(Zerg.ZerglingSpeed))),
    new If(new ProceedWithDrones, new Pump(Zerg.Drone, 16)),

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
          new BuildGasPumps(5))),
      new If(new ProceedWithDrones, new Pump(Zerg.Drone, 24))),
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
      new Pump(Zerg.Drone, 16)),
    new Pump(Zerg.Zergling, 4),
    new If(
      new ProceedWithDrones,
      new Pump(Zerg.Drone, 24)),
    new IfOnMiningBases(3, new Pump(Zerg.Drone, 24)),
    new IfOnMiningBases(4, new Pump(Zerg.Drone, 32)),
    new IfOnMiningBases(5, new Pump(Zerg.Drone, 40)),
    new IfOnMiningBases(6, new Pump(Zerg.Drone, 48)),
        
    new Pump(Zerg.Zergling)
  )
}
