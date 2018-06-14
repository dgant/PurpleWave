package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.{Get, GetAnother}
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Matchup.EnemyIsTerran
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{Employing, OnMap, StartPositionsAtMost}
import Planning.Plans.Scouting.{CampExpansions, FoundEnemyBase}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Zerg.ZvPNinePool
import Strategery.Transistor

class ZvPNinePool extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(ZvPNinePool)
  override def defaultScoutPlan: Plan = new If(
    new Or(
      new StartPositionsAtMost(2),
      new Not(new FoundEnemyBase)),
    new ScoutSafelyWithOverlord)
  
  override def priorityAttackPlan: Plan = new If(
    new UnitsAtLeast(24, UnitMatchWarriors),
    new CampExpansions)
  
  override def defaultAggressionPlan: Plan = new If(
    new UnitsAtLeast(40, Zerg.Hydralisk),
    new Aggression(4.0),
    new If(
      new UnitsAtLeast(30, Zerg.Hydralisk),
      new Aggression(2.2),
      new If(
        new UnitsAtLeast(15, Zerg.Hydralisk),
        new Aggression(1.5),
        new Aggression(1.15))))
  
  override def defaultAttackPlan: Plan = new Attack
  
  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(1, Zerg.SpawningPool)),
    new If(
      new UnitsAtLeast(1, Zerg.SpawningPool),
      new Trigger(
        new UnitsAtLeast(10, Zerg.Drone),
        initialBefore = new ExtractorTrick)),
    new BuildOrder(
      Get(11, Zerg.Drone),
      Get(1, Zerg.Overlord),
      Get(6, Zerg.Zergling)))
  
  private class TrainJustEnoughZerglings extends TrainMatchingRatio(
    Zerg.Zergling, 2, 12,
    Seq(
      Enemy(Terran.Marine, 1.5),
      Enemy(Terran.Medic, 3.0),
      Enemy(Terran.Firebat, 3.0),
      Enemy(Protoss.Zealot, 4.5),
      Enemy(Protoss.Dragoon, 3.0),
      Enemy(Zerg.Zergling, 1.5)))
  
  private class TakeSecondGasForMuta extends If(
    new And(
      new UnitsAtLeast(1, Zerg.Lair),
      new Or(
        new UnitsAtLeast(15, Zerg.Drone, countEggs = true),
        new MineralsAtLeast(250))),
    new BuildGasPumps(2))
  
  private class TakeThirdGasForMuta extends If(
    new And(
      new UnitsAtLeast(1, Zerg.Lair),
      new Or(
        new MineralsAtLeast(550),
        new UnitsAtLeast(21, Zerg.Drone, countEggs = true))),
    new BuildGasPumps)
  
  private class NeedTechTransition extends Or(
    new TwoBaseProtoss,
    new And(
      new EnemyIsTerran,
      new EnemiesAtLeast(1, Terran.Bunker, complete = true)))
  
  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
  
        // Post-transition: 3 Hatch Hydra
        new If(new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true), new Parallel(
          new CapGasAt(200),
          new Pump(Zerg.Drone, 12),
          new Build(Get(1, Zerg.SpawningPool), Get(1, Zerg.Extractor), Get(1, Zerg.HydraliskDen)),
          new UpgradeContinuously(Zerg.HydraliskSpeed),
          new TrainMatchingRatio(Zerg.Hydralisk, 0, 4, Seq(Enemy(Protoss.Zealot, 1.0))),
          new If(
            new UpgradeComplete(Zerg.HydraliskSpeed),
            new UpgradeContinuously(Zerg.HydraliskRange)),
          new Pump(Zerg.Drone, 22),
          new If(new UnitsAtLeast(18, Zerg.Drone), new BuildGasPumps(2)),
          new Pump(Zerg.Hydralisk),
          new If(
            new MineralsAtLeast(400),
            new Build(GetAnother(1, Zerg.Hatchery)))
        )),
  
        // 2/3 Hatch Muta
        new If(new UnitsAtLeast(1, Zerg.Spire, complete = true), new Parallel(
          new CapGasAtRatioToMinerals(1.0, 100),
          // TODO: Need to add Overlords here so we have enough supply for banked Mutalisks
          new BuildOrder(Get(6, Zerg.Mutalisk)),
          new If(
            new UnitsAtLeast(6, Zerg.Mutalisk, complete = true),
            new Pump(Zerg.Drone, 21)),
          new Pump(Zerg.Drone, 16),
          new Build(Get(1, Zerg.SpawningPool), Get(1, Zerg.Extractor)),
          new Build(Get(1, Zerg.Lair)),
          new Build(Get(1, Zerg.Spire), Get(2, Zerg.Extractor)),
          new Trigger(
            new EnemiesAtLeast(1, UnitMatchOr(
              Terran.Wraith,
              Terran.Valkyrie,
              Terran.Starport,
              Protoss.Corsair,
              Protoss.Stargate,
              Zerg.Mutalisk,
              Zerg.Scourge,
              Zerg.Spire)),
            new TrainMatchingRatio(Zerg.Scourge, 2, 12, Seq(
              Enemy(Terran.Wraith, 2.0),
              Enemy(Terran.Valkyrie, 2.0),
              Enemy(Protoss.Corsair, 2.0),
              Enemy(Protoss.Scout, 3.0),
              Enemy(Zerg.Mutalisk, 2.0),
              Enemy(Zerg.Scourge, 1.0)))),
          new If(
            new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
            new Pump(Zerg.Mutalisk)),
          new Pump(Zerg.Zergling),
          new RequireMiningBases(3)
        )),
  
      // Pre-transition: 2-Hatch Speedlings
      new If(new UnitsAtMost(0, UnitMatchOr(Zerg.HydraliskDen, Zerg.Spire), complete = true), new Parallel(
        new RequireMiningBases(2),
        new Pump(Zerg.Drone, 11),

        new If(
          new NeedTechTransition,
          new If(
            new OnMap(Transistor),
            
            // Transition to 2-Hatch Muta
            new Parallel(
              new CapGasAtRatioToMinerals(1.0, 100),
              new Pump(Zerg.Drone, 11),
              new RequireBases(2),
              new If(
                new UnitsAtLeast(1, Zerg.Spire),
                new Parallel(
                  new Pump(Zerg.Drone, 18),
                  new BuildOrder(Get(6, Zerg.Mutalisk)))), // Won't actually happen but ensures we save the larvae
              new BuildGasPumps(1),
              new TakeSecondGasForMuta,
              new TakeThirdGasForMuta,
              new TrainJustEnoughZerglings,
              new Pump(Zerg.Drone, 25),
              new If(
                new UnitsAtLeast(1, Zerg.Extractor, complete = true),
                new Parallel(
                  new Build(Get(1, Zerg.Lair)),
                  new Build(
                    Get(Zerg.ZerglingSpeed),
                    Get(1, Zerg.Spire)))),
              new RequireBases(3),
              new Pump(Zerg.Zergling),
              new Trigger(
                new And(
                  new MineralsAtLeast(800),
                  new UnitsAtLeast(1, Zerg.Spire, complete = true)),
                new RequireBases(4))
            ),
            
            // Transition to 3-Hatch Hydra
            new Parallel(
              new If(
                new UnitsExactly(0, Zerg.HydraliskDen),
                new CapGasAt(50),
                new CapGasAt(175)),
              new Pump(Zerg.Drone, 13),
              new RequireBases(3),
              new Build(
                Get(1, Zerg.Extractor),
                Get(1, Zerg.HydraliskDen)),
              new TrainJustEnoughZerglings,
              new Pump(Zerg.Drone))),

          // Transition to 3-Hatch Muta
          new Parallel(
            new If(
              new UnitsAtMost(0, Zerg.Lair),
              new CapGasAt(100),
              new If(
                new UnitsAtMost(0, Zerg.Spire),
                new CapGasAt(200),
                new CapGasAtRatioToMinerals(1.0, 100))),
            new If(
              new UpgradeComplete(Zerg.ZerglingSpeed),
              new Pump(Zerg.Drone, 16)),
            new Pump(Zerg.Drone, 11),
            new BuildOrder(
              Get(12, Zerg.Zergling),
              Get(13, Zerg.Drone)),
            new Pump(Zerg.Zergling),
            new RequireBases(3),
            new If(
              new Or(new MiningBasesAtLeast(3), new MineralsAtLeast(350)),
              new BuildGasPumps(1)),
            new TakeSecondGasForMuta,
            new TakeThirdGasForMuta,
            new Build(
              Get(1, Zerg.Extractor),
              Get(Zerg.ZerglingSpeed)),
            new Build(Get(1, Zerg.Lair)),
            new Build(Get(1, Zerg.Spire)))
      )))
    )))
  
}

