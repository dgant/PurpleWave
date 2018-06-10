package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast}
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{ExtractorTrick, MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.{Employing, OnMap, StartPositionsAtMost}
import Planning.Plans.Scouting.{CampExpansions, FoundEnemyBase}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Maps.Transistor
import Strategery.Strategies.Zerg.ZvPNinePool

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
      new UnitsAtLeast(40, UnitMatchWarriors),
      new Aggression(4.0),
      new If(
        new UnitsAtLeast(30, UnitMatchWarriors),
        new Aggression(2.2),
        new If(
          new UnitsAtLeast(16, UnitMatchWarriors),
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
  
  private class TrainJustEnoughZerglings extends TrainMatchingRatio(Zerg.Zergling, 1, 12, Seq(MatchingRatio(Protoss.Zealot, 4.0)))
  private class TwoHatchMutaTakeSecondGas extends If(
    new And(
      new UnitsAtLeast(1, Zerg.Spire),
      new Or(
        new UnitsAtLeast(14, Zerg.Drone, countEggs = true),
        new MineralsAtLeast(250))),
    new BuildGasPumps)
  
  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
  
        // Post-transition: 3 Hatch Hydra
        new Trigger(
          new Or(
            new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true),
            new UnitsAtLeast(1, Zerg.Spire, complete = true)),
          new Trigger(
            new UnitsAtLeast(1, Zerg.HydraliskDen),
            
            // 3 Hatch Hydra
            new Parallel(
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
            ),
            
            // 2 Hatch Muta
            new Parallel(
              new If(
                new UnitsAtLeast(16, Zerg.Drone),
                new CapGasAt(500),
                new CapGasAt(300)),
              new TrainContinuously(Zerg.Drone, 12),
              new Build(RequestAtLeast(1, Zerg.SpawningPool), RequestAtLeast(1, Zerg.Extractor), RequestAtLeast(1, Zerg.Lair), RequestAtLeast(1, Zerg.Spire)),
              new TwoHatchMutaTakeSecondGas,
              new Trigger(
                new EnemyUnitsAtLeast(1, UnitMatchOr(Protoss.Corsair, Protoss.Stargate)),
                new TrainMatchingRatio(Zerg.Scourge, 2, 12, Seq(MatchingRatio(Protoss.Corsair, 2.0)))),
              new If(
                new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
                new TrainContinuously(Zerg.Mutalisk)),
              new TrainContinuously(Zerg.Drone, 18),
              new TrainContinuously(Zerg.Zergling),
              new RequireMiningBases(3)
            ))),
      
          // Pre-transition: 3-Hatch Speedlings
          new Parallel(
            new RequireMiningBases(2),
            new TrainContinuously(Zerg.Drone, 11),
  
            new If(
              new And(
                new TwoBaseProtoss,
                new Not(new OnMap(Transistor))),
              
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
                new TrainContinuously(Zerg.Drone)),
  
              // Vomit Zerglings while transitioning to 2-Hatch Muta
              new Parallel(
                new If(
                  new UnitsAtMost(0, Zerg.Spire),
                  new CapGasAt(200),
                  new CapGasAt(700)),
                new If(
                  new UnitsAtLeast(12, Zerg.Zergling),
                  new TrainContinuously(Zerg.Drone, 15)),
                new If(
                  new UnitsAtLeast(1, Zerg.Spire),
                  new Parallel(
                    new TrainContinuously(Zerg.Drone, 18),
                    new BuildOrder(RequestAtLeast(6, Zerg.Mutalisk)))), // Won't actually happen but ensures we save the larvae
                new TrainContinuously(Zerg.Zergling, 16),
                new RequireBases(2),
                new BuildGasPumps(1),
                new FlipIf(
                  new TwoBaseProtoss,
                  new UpgradeContinuously(Zerg.ZerglingSpeed),
                  new Build(RequestAtLeast(1, Zerg.Lair))),
                new Build(RequestAtLeast(1, Zerg.Spire)),
                new TwoHatchMutaTakeSecondGas,
                new TrainContinuously(Zerg.Drone, 18),
                new TrainContinuously(Zerg.Zergling))
      )))))
  
}

