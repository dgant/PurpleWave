package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.Requests.{Get, RequestBuildable}
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.PumpMutalisks
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Placement.BuildSunkensInMain
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters.IsHatchlike
import Planning.Plan
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ9PoolSpeed
import Tactic.Tactics.DefendFightersAgainstRush

class ZvZ9PoolSpeed extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ9PoolSpeed)

  override def scoutPlan: Plan = NoPlan()
  
  override def attackPlan: Plan = new If(
    new Or(
      new And(
        new Not(new EnemyStrategy(With.fingerprints.fourPool)),
        new Not(new EnemyStrategy(With.fingerprints.ninePoolGas))),
      new And(
        new UpgradeComplete(Zerg.ZerglingSpeed),
        new Not(new EnemyHasUpgrade(Zerg.ZerglingSpeed))),
      new UnitsAtLeast(3, Zerg.Mutalisk, complete = true)),
    new AttackAndHarass)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[RequestBuildable] = Vector(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(2, Zerg.Overlord),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling))
  
  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAtRatioToMinerals(1.0, 50),
    new If(
      new Not(new UnitsAtLeast(1, Zerg.Spire)),
      new If(
        new UnitsAtLeast(1, Zerg.Lair),
        new CapGasAt(150),
        new If(
          new GasForUpgrade(Zerg.ZerglingSpeed),
            new CapGasWorkersAt(2)))),

    new Pump(Zerg.SunkenColony),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new BuildSunkensInMain(1)),

    new If(
      new GasAtLeast(100),
      new Build(
        Get(Zerg.ZerglingSpeed),
        Get(Zerg.Lair))),

    // Against 2-Hatch builds, add Sunkens to survive Zergling pressure
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.Lair),
        new Or(
          new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool),
          new EnemiesAtLeast(2, IsHatchlike, complete = true))),
        new Parallel(
          // Finish our initial Zergling pressure.
          // The tenth Zergling arrives as 12 Hatch's first Zerglings pop
          // Anything past twelve is unlikely to contribute to pressure
          new BuildOrder(Get(12, Zerg.Zergling)),
          // We only need to end on 8 drones to sustain Mutalisk production,
          // in order to have the extra money for Sunkens we'd like to start by overbuilding Drones such that we end on 9
          // Overbuilding Drones vs. Pool-first builds (into 2 Hatch) is suicide though; we win this automatically with Mutalisks anyhow
          new If(new Not(new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.overpool)), new BuildOrder(Get(Zerg.Drone, 14))),
          new Pump(Zerg.Drone, 7),
          new BuildSunkensInMain(2))),

    new Pump(Zerg.Drone, 8),
    new Build(Get(Zerg.Spire)),
    new If(new UnitsAtLeast(1, Zerg.Spire), new BuildOrder(Get(3, Zerg.Mutalisk))),
    new PumpMutalisks,
    new Pump(Zerg.Zergling),
  )
}
