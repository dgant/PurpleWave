package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.Requests.Get
import Planning.Plans.Army.{Aggression, AttackAndHarass}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Placement.BuildSunkensAtNatural
import Planning.Predicates.Compound._
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.MacroFacts
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters.{IsAll, IsComplete, IsAny}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Zerg.ZvP2HatchMuta

class ZvP2HatchMuta extends GameplanTemplate {
  
  override val activationCriteria: Predicate = Employing(ZvP2HatchMuta)
  
  object ProceedWithTech extends Or(
    Latch(UnitsAtLeast(14, Zerg.Drone)),
    Latch(UnitsAtLeast(8, Zerg.Zergling)),
    Latch(UnitsAtLeast(2, Zerg.SunkenColony)),
    Not(EnemyStrategy(
      With.fingerprints.twoGate,
      With.fingerprints.proxyGateway)))
  
  object ProceedWithDrones extends Or(
    ProceedWithTech,
    Latch(UnitsAtLeast(1, Zerg.Zergling)))

  object ShouldDoSpeedlingAllIn extends EnemyStrategy(
    With.fingerprints.cannonRush,
    With.fingerprints.proxyGateway)

  class DoSpeedlingAllIn extends Parallel(
    new Aggression(1.2),
    new BuildOrder(Get(10, Zerg.Zergling)),
    new If(
      new Or(
        GasAtLeast(100),
        UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1))),
      new CapGasAt(0)),
    new FlipIf(
      new SafeAtHome,
      new Pump(Zerg.Zergling),
      new Pump(Zerg.Drone, 9)),
    new Build(
      Get(1, Zerg.Extractor),
      Get(Zerg.ZerglingSpeed)),
    new RequireMiningBases(3),
    new If(
      MineralsAtLeast(400),
      new RequireMiningBases(4)))
  
  private def sunkenProfile = PlacementProfiles.defensive

  override def attackPlan: Plan = new If(
    new Or(
      UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      UpgradeComplete(Zerg.ZerglingSpeed),
      ShouldDoSpeedlingAllIn,
      EnemyBasesAtLeast(2)),
    new AttackAndHarass)

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
      EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.nexusFirst),
      new Parallel(
        new BuildOrder(
          Get(17, Zerg.Drone),
          Get(Zerg.Lair),
          Get(21, Zerg.Drone),
          Get(3, Zerg.Overlord),
          Get(3, Zerg.Hatchery),
          Get(22, Zerg.Drone),
          Get(Zerg.Spire),
          Get(24, Zerg.Drone)))), // 26 if we never made any Zerglings accidentally along the way
    new If(
      EnemyStrategy(With.fingerprints.gatewayFe),
      new Parallel(
        new BuildOrder(
          Get(8, Zerg.Zergling),
          Get(Zerg.Lair),
          Get(17, Zerg.Drone)),
        new BuildSunkensAtNatural(2),
        new BuildOrder(
          Get(19, Zerg.Drone),
          Get(3, Zerg.Overlord),
          Get(Zerg.Spire),
          Get(3, Zerg.Hatchery),
          Get(22, Zerg.Drone)))))
    
  lazy val ZealotOrDragoon = IsAny(Protoss.Zealot, Protoss.Dragoon)
  
  class ReactiveSunkensVsZealots extends If(
    And(
      UnitsAtLeast(12, Zerg.Drone),
      UnitsAtMost(0, Zerg.Spire, complete = true)),
    new If(
      EnemiesAtLeast(17, ZealotOrDragoon),
      new BuildSunkensAtNatural(7, sunkenProfile),
      new If(
        EnemiesAtLeast(13, ZealotOrDragoon),
        new BuildSunkensAtNatural(6, sunkenProfile),
        new If(
          EnemiesAtLeast(10, ZealotOrDragoon),
          new BuildSunkensAtNatural(5, sunkenProfile),
          new If(
            EnemiesAtLeast(7, ZealotOrDragoon),
            new BuildSunkensAtNatural(4, sunkenProfile),
            new If(
              EnemiesAtLeast(5, ZealotOrDragoon),
              new BuildSunkensAtNatural(3, sunkenProfile),
              new If(
                EnemiesAtLeast(3, ZealotOrDragoon),
                new BuildSunkensAtNatural(2, sunkenProfile),
                new If(
                  EnemyStrategy(With.fingerprints.twoGate),
                  new BuildSunkensAtNatural(2, sunkenProfile)))))))))
  
  class ReactiveZerglings extends If(
    UnitsAtMost(0, Zerg.Spire, complete = true),
    new PumpRatio(Zerg.Zergling, 0, 10, Seq(
      Friendly(IsAll(Zerg.SunkenColony, IsComplete), -6.0),
      Enemy(Terran.Marine, 1.5),
      Enemy(Terran.Firebat, 3.0),
      Enemy(Terran.Vulture, 2.0),
      Enemy(Protoss.Zealot, 4.0),
      Enemy(Zerg.Zergling, 1.75))))
  
  def NeedExpansion(droneCount: Int) = Check(() => MacroFacts.units(Zerg.Drone) >= droneCount || MacroFacts.minerals >= 500)
  
  object ReadyToExpand extends Or(
    UnitsAtLeast(1, Zerg.Spire, complete = true),
    MineralsAtLeast(300))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new Pump(Zerg.SunkenColony),
    new PumpRatio(Zerg.Drone, 0, 18, Seq(Flat(9), Friendly(IsAll(Zerg.SunkenColony, IsComplete), 3))),
    new ReactiveSunkensVsZealots,
    new ZergReactionVsWorkerRush
  )
  
  override def buildPlans: Seq[Plan] = Vector(
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
      new Not(ProceedWithTech)),
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
        Enemy(IsAny(Terran.Valkyrie, Terran.Wraith, Protoss.Corsair, Protoss.Stargate, Zerg.Mutalisk), 2.0),
        Enemy(IsAny(Zerg.Scourge), 1.0))),
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
      ShouldDoSpeedlingAllIn,
      new DoSpeedlingAllIn),
    
    new ReactiveZerglings,
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new BuildOrder(
        Get(5, Zerg.Overlord),
        Get(8, Zerg.Mutalisk))), // make sure we have enough when mutas pop

    new Pump(Zerg.Drone, 9),
    new If(ProceedWithDrones, new Pump(Zerg.Drone, 12)),
    new If(
      ProceedWithTech,
      new Build(
        Get(Zerg.Lair),
        Get(Zerg.Spire),
        Get(Zerg.ZerglingSpeed))),
    new If(ProceedWithDrones, new Pump(Zerg.Drone, 16)),

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
      new If(ProceedWithDrones, new Pump(Zerg.Drone, 24))),
    new If(
      new And(
        NeedExpansion(20),
        ReadyToExpand),
      new RequireMiningBases(3)),
    new If(
      new And(
        NeedExpansion(26),
        ReadyToExpand),
      new RequireBases(4)),
    new If(
      new And(
        NeedExpansion(32),
        ReadyToExpand),
      new RequireBases(5)),
    new If(
      new And(
        NeedExpansion(40),
        ReadyToExpand),
      new RequireBases(6)),
    new If(
      new And(
        NeedExpansion(50),
        ReadyToExpand),
      new RequireBases(7)),
    new If(
      ProceedWithDrones,
      new Pump(Zerg.Drone, 16)),
    new Pump(Zerg.Zergling, 4),
    new If(
      ProceedWithDrones,
      new Pump(Zerg.Drone, 24)),
    new IfOnMiningBases(3, new Pump(Zerg.Drone, 24)),
    new IfOnMiningBases(4, new Pump(Zerg.Drone, 32)),
    new IfOnMiningBases(5, new Pump(Zerg.Drone, 40)),
    new IfOnMiningBases(6, new Pump(Zerg.Drone, 48)),
        
    new Pump(Zerg.Zergling)
  )
}
