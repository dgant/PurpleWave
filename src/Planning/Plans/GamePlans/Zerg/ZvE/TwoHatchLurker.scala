package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{ExtractorTrick, MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Zerg.{BuildSunkensAtExpansions, BuildSunkensAtNatural}
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{Employing, StartPositionsAtLeast}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.TwoHatchLurker

class TwoHatchLurker extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TwoHatchLurker)
  
  override def defaultScoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new If(
      new StartPositionsAtLeast(3),
      new Trigger(
        new Or(
          new UnitsAtLeast(2, Zerg.Hatchery),
          new MineralsAtLeast(300)),
        new ScoutSafelyWithDrone)))
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new Not(new OneBaseProtoss),
      new Latch(new UnitsAtLeast(1, Zerg.Lurker, complete = true))),
    new Attack)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new TrainContinuously(Zerg.SunkenColony),
    new BuildOrder(RequestAtLeast(9, Zerg.Drone)),
    new If(
      new And(
        new UnitsAtMost(1, Zerg.Hatchery),
        new MineralsAtLeast(80),
        new UnitsAtLeast(1, Zerg.Larva)),
      new ExtractorTrick),
    new BuildOrder(
      RequestAtLeast(10, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(12, Zerg.Drone)),
    new RequireMiningBases(2),
    new BuildOrder(
      RequestAtLeast(13, Zerg.Drone), // Greed
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(16, Zerg.Drone)),
    new If(
      new SupplyOutOf200(14),
      new BuildGasPumps(1)),
    new Trigger(
      new UnitsAtLeast(1, Zerg.Lair, complete = true),
      new BuildGasPumps),
    new If(
      new EnemyStrategy(With.intelligence.fingerprints.gatewayFe),
      new Parallel(
        new BuildOrder(RequestAtLeast(6, Zerg.Zergling)),
        new Trigger(new UnitsAtLeast(1, Zerg.Zergling), new BuildOrder(RequestAtLeast(18, Zerg.Drone))),
        new BuildOrder(RequestAtLeast(8, Zerg.Zergling))),
      new If(
        new TwoBaseProtoss,
        new BuildOrder(RequestAtLeast(22, Zerg.Drone)),
        new Parallel(
          new BuildOrder(RequestAtLeast(8, Zerg.Zergling)),
          new Trigger(new UnitsAtLeast(1, Zerg.Lurker, complete = true), initialBefore = new BuildSunkensAtNatural(2)),
          new Trigger(
            new UnitsAtLeast(1, Zerg.Zergling),
            new BuildOrder(RequestAtLeast(19, Zerg.Drone)))))))
  
  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAt(300),
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool),
      new Parallel(
        new Trigger(
          new EnemyUnitsAtLeast(1, Protoss.Corsair),
          new TrainMatchingRatio(Zerg.Scourge, 2, 10, Seq(MatchingRatio(Protoss.Corsair, 2.0)))),
          
        new If(
          new UnitsAtLeast(1, Zerg.HydraliskDen),
          new Parallel(
            new BuildOrder(RequestTech(Zerg.LurkerMorph)),
            new If(
              new And(
                new OneBaseProtoss,
                new Not(new Latch(new UnitsAtLeast(1, Zerg.Lurker, complete = true)))),
              new BuildSunkensAtNatural(3)))),
        
        new If(
          new TechComplete(Zerg.LurkerMorph, Zerg.Hydralisk.buildFrames),
          new BuildOrder(RequestAtLeast(3, Zerg.Hydralisk))),
        
        new TrainContinuously(Zerg.Lurker),
        new TrainContinuously(Zerg.Hydralisk, 1, 1),
        new If(
          new TechComplete(Zerg.LurkerMorph),
          new TrainMatchingRatio(Zerg.Zergling, 4, 18, Seq(
            MatchingRatio(Protoss.Dragoon, 2.0))),
          new TrainMatchingRatio(Zerg.Zergling, 4, 18, Seq(
            MatchingRatio(Protoss.Zealot, 2.5),
            MatchingRatio(Protoss.Dragoon, 2.0)
          ))),
  
        new Trigger(
          new UnitsAtLeast(1, Zerg.Lurker, complete = true),
          new Parallel(
            new ExpandAtDrones(16, 3),
            new ExpandAtDrones(22, 4),
            new MacroHatchAtDrones(25, 5),
            new MacroHatchAtDrones(30, 6),
            new MacroHatchAtDrones(35, 7))),
  
        new TrainContinuously(Zerg.Mutalisk, 6),
        
        new If(
          new MiningBasesAtLeast(3),
          new Build(
            RequestAtLeast(1, Zerg.QueensNest),
            RequestAtLeast(1, Zerg.Hive),
            RequestUpgrade(Zerg.ZerglingAttackSpeed))),
        
        new If(
          new GasAtLeast(100),
          new TrainContinuously(Zerg.Mutalisk)),
        
        new BuildSunkensAtExpansions(3),
        new Build(
          RequestAtLeast(1, Zerg.Lair),
          RequestUpgrade(Zerg.ZerglingSpeed),
          RequestAtLeast(1, Zerg.HydraliskDen)),
  
        new Trigger(
          new UnitsAtLeast(3, Zerg.Lurker, complete = true),
          new Build(RequestAtLeast(1, Zerg.Spire))),
        
        new TrainContinuously(Zerg.Drone, 26),
        new TrainContinuously(Zerg.Zergling)
      )))
}
