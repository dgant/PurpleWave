package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{Parallel, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{ExtractorTrick, MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Scouting.{CampExpansions, FoundEnemyBase}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvPNinePoolThreeHatch

class ZvPNinePoolThreeHatch extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(ZvPNinePoolThreeHatch)
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
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
          new Aggression(1.6),
          new Aggression(1.3)))))
  
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
  
  override def buildPlans: Seq[Plan] = Seq(
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
              new CapGasAt(400),
              new TrainContinuously(Zerg.Drone, 12),
              new Build(RequestAtLeast(1, Zerg.SpawningPool), RequestAtLeast(1, Zerg.Extractor), RequestAtLeast(1, Zerg.Lair), RequestAtLeast(1, Zerg.Spire)),
              new If(
                new Or(
                  new UnitsAtLeast(14, Zerg.Drone, countEggs = true),
                  new MineralsAtLeast(400)),
                new BuildGasPumps),
              new Trigger(
                new EnemyUnitsAtLeast(1, UnitMatchOr(Protoss.Corsair, Protoss.Stargate)),
                new TrainMatchingRatio(Zerg.Scourge, 2, 12, Seq(MatchingRatio(Protoss.Corsair, 2.0)))),
              new If(
                new Check(() => With.self.gas > Math.min(100, With.self.minerals)),
                new TrainContinuously(Zerg.Mutalisk)),
              new TrainContinuously(Zerg.Drone, 16),
              new TrainContinuously(Zerg.Zergling),
              new RequireMiningBases(3)
            ))),
      
          // Pre-transition: 3-Hatch Speedlings
          new Parallel(
            new RequireMiningBases(2),
            new TrainContinuously(Zerg.Drone, 11),
  
            new If(
              new TwoBaseProtoss,
              
              // Transition to 3-Hatch Hydra
              new Parallel(
                new CapGasAt(150),
                new TrainContinuously(Zerg.Drone, 13),
                new RequireBases(3),
                new Build(
                  RequestAtLeast(1, Zerg.Extractor),
                  RequestAtLeast(1, Zerg.HydraliskDen)),
                new TrainContinuously(Zerg.Drone)),
  
              // Vomit Zerglings while transitioning to 2-Hatch Muta
              new Parallel(
                new CapGasAt(200),
                new If(
                  new UnitsAtLeast(15, Zerg.Zergling),
                  new TrainContinuously(Zerg.Drone, 15)),
                new TrainContinuously(Zerg.Zergling),
                new RequireBases(2),
                new Build(
                  RequestAtLeast(1, Zerg.Extractor),
                  RequestUpgrade(Zerg.ZerglingSpeed),
                  RequestAtLeast(1, Zerg.Lair),
                  RequestAtLeast(1, Zerg.Spire)))
      )))))
  
}

