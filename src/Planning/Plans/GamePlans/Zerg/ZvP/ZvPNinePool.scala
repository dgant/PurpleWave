package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
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
    new If(
      new UnitsAtLeast(40, Zerg.Hydralisk),
      new Aggression(4.0),
      new If(
        new UnitsAtLeast(30, Zerg.Hydralisk),
        new Aggression(2.2),
        new If(
          new UnitsAtLeast(15, Zerg.Hydralisk),
          new Aggression(1.5),
          new Aggression(1.15)))))
  
  override def defaultAttackPlan: Plan = new Attack
  
  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(1, Zerg.SpawningPool)),
    new If(
      new UnitsAtLeast(1, Zerg.SpawningPool),
      new Trigger(
        new UnitsAtLeast(10, Zerg.Drone),
        initialBefore = new ExtractorTrick)),
    new BuildOrder(
      RequestAtLeast(11, Zerg.Drone),
      RequestAtLeast(1, Zerg.Overlord),
      RequestAtLeast(6, Zerg.Zergling)))
  
  private class TrainJustEnoughZerglings extends TrainMatchingRatio(
    Zerg.Zergling, 2, 12,
    Seq(
      MatchingRatio(Terran.Marine, 1.5),
      MatchingRatio(Terran.Medic, 3.0),
      MatchingRatio(Terran.Firebat, 3.0),
      MatchingRatio(Protoss.Zealot, 4.5),
      MatchingRatio(Protoss.Dragoon, 3.0),
      MatchingRatio(Zerg.Zergling, 1.5)))
  
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
      new EnemyUnitsAtLeast(1, Terran.Bunker, complete = true)))
  
  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
  
        // Post-transition: 3 Hatch Hydra
        new If(new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true), new Parallel(
          new CapGasAt(200),
          new TrainContinuously(Zerg.Drone, 12),
          new Build(RequestAtLeast(1, Zerg.SpawningPool), RequestAtLeast(1, Zerg.Extractor), RequestAtLeast(1, Zerg.HydraliskDen)),
          new UpgradeContinuously(Zerg.HydraliskSpeed),
          new TrainMatchingRatio(Zerg.Hydralisk, 0, 4, Seq(MatchingRatio(Protoss.Zealot, 1.0))),
          new If(
            new UpgradeComplete(Zerg.HydraliskSpeed),
            new UpgradeContinuously(Zerg.HydraliskRange)),
          new TrainContinuously(Zerg.Drone, 22),
          new If(new UnitsAtLeast(18, Zerg.Drone), new BuildGasPumps(2)),
          new TrainContinuously(Zerg.Hydralisk),
          new If(
            new MineralsAtLeast(400),
            new Build(RequestAnother(1, Zerg.Hatchery)))
        )),
  
        // 2/3 Hatch Muta
        new If(new UnitsAtLeast(1, Zerg.Spire, complete = true), new Parallel(
          new CapGasAtRatioToMinerals(1.0, 100),
          // TODO: Need to add Overlords here so we have enough supply for banked Mutalisks
          new BuildOrder(RequestAtLeast(6, Zerg.Mutalisk)),
          new If(
            new UnitsAtLeast(6, Zerg.Mutalisk, complete = true),
            new TrainContinuously(Zerg.Drone, 21)),
          new TrainContinuously(Zerg.Drone, 16),
          new Build(RequestAtLeast(1, Zerg.SpawningPool), RequestAtLeast(1, Zerg.Extractor)),
          new Build(RequestAtLeast(1, Zerg.Lair)),
          new Build(RequestAtLeast(1, Zerg.Spire), RequestAtLeast(2, Zerg.Extractor)),
          new Trigger(
            new EnemyUnitsAtLeast(1, UnitMatchOr(
              Terran.Wraith,
              Terran.Valkyrie,
              Terran.Starport,
              Protoss.Corsair,
              Protoss.Stargate,
              Zerg.Mutalisk,
              Zerg.Scourge,
              Zerg.Spire)),
            new TrainMatchingRatio(Zerg.Scourge, 2, 12, Seq(
              MatchingRatio(Terran.Wraith, 2.0),
              MatchingRatio(Terran.Valkyrie, 2.0),
              MatchingRatio(Protoss.Corsair, 2.0),
              MatchingRatio(Protoss.Scout, 3.0),
              MatchingRatio(Zerg.Mutalisk, 2.0),
              MatchingRatio(Zerg.Scourge, 1.0)))),
          new If(
            new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
            new TrainContinuously(Zerg.Mutalisk)),
          new TrainContinuously(Zerg.Zergling),
          new RequireMiningBases(3)
        )),
  
      // Pre-transition: 2-Hatch Speedlings
      new If(new UnitsAtMost(0, UnitMatchOr(Zerg.HydraliskDen, Zerg.Spire), complete = true), new Parallel(
        new RequireMiningBases(2),
        new TrainContinuously(Zerg.Drone, 11),

        new If(
          new NeedTechTransition,
          new If(
            new OnMap(Transistor),
            
            // Transition to 2-Hatch Muta
            new Parallel(
              new CapGasAtRatioToMinerals(1.0, 100),
              new TrainContinuously(Zerg.Drone, 11),
              new RequireBases(2),
              new If(
                new UnitsAtLeast(1, Zerg.Spire),
                new Parallel(
                  new TrainContinuously(Zerg.Drone, 18),
                  new BuildOrder(RequestAtLeast(6, Zerg.Mutalisk)))), // Won't actually happen but ensures we save the larvae
              new BuildGasPumps(1),
              new TakeSecondGasForMuta,
              new TakeThirdGasForMuta,
              new TrainJustEnoughZerglings,
              new TrainContinuously(Zerg.Drone, 25),
              new If(
                new UnitsAtLeast(1, Zerg.Extractor, complete = true),
                new Parallel(
                  new Build(RequestAtLeast(1, Zerg.Lair)),
                  new Build(
                    RequestUpgrade(Zerg.ZerglingSpeed),
                    RequestAtLeast(1, Zerg.Spire)))),
              new RequireBases(3),
              new TrainContinuously(Zerg.Zergling),
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
              new TrainContinuously(Zerg.Drone, 13),
              new RequireBases(3),
              new Build(
                RequestAtLeast(1, Zerg.Extractor),
                RequestAtLeast(1, Zerg.HydraliskDen)),
              new TrainJustEnoughZerglings,
              new TrainContinuously(Zerg.Drone))),

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
              new TrainContinuously(Zerg.Drone, 16)),
            new TrainContinuously(Zerg.Drone, 11),
            new BuildOrder(
              RequestAtLeast(12, Zerg.Zergling),
              RequestAtLeast(13, Zerg.Drone)),
            new TrainContinuously(Zerg.Zergling),
            new RequireBases(3),
            new If(
              new Or(new MiningBasesAtLeast(3), new MineralsAtLeast(350)),
              new BuildGasPumps(1)),
            new TakeSecondGasForMuta,
            new TakeThirdGasForMuta,
            new Build(
              RequestAtLeast(1, Zerg.Extractor),
              RequestUpgrade(Zerg.ZerglingSpeed)),
            new Build(RequestAtLeast(1, Zerg.Lair)),
            new Build(RequestAtLeast(1, Zerg.Spire)))
      )))
    )))
  
}

