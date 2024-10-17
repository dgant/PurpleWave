package Planning.Plans.Gameplans.Zerg.ZvT

import Lifecycle.With
import Macro.Requests.{Get, RequestBuildable}
import Planning.Plans.Army.{AllInIf, AttackAndHarass}
import Planning.Plans.Basic.Write
import Planning.Plans.Compound._
import Planning.Plans.Gameplans.Zerg.ZergIdeas.UpgradeHydraRangeThenSpeed
import Planning.Plans.Gameplans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.ScoutNow
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.Plan
import Planning.Plans.Gameplans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT3HatchLing
import Utilities.Time.GameTime

class ZvT3HatchLing extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT3HatchLing)

  override def attackPlan: Plan = new Parallel(
    new If(new UpgradeComplete(Zerg.HydraliskRange), new AttackAndHarass),
    new If(
      new Or(
        new EnemiesAtMost(0, Terran.Factory),
        new UpgradeComplete(Zerg.ZerglingSpeed)),
      new AttackAndHarass))

  override def scoutPlan: Plan = new If(
    new And(
      new Not(EnemyWalledIn),
      new Not(new EnemyStrategy(With.fingerprints.twoFacVultures))),
    new Trigger(
      new Or(
        new And(
          new StartPositionsAtLeast(4),
          new MineralsForUnit(Zerg.Overlord, 2)),
        new MineralsForUnit(Zerg.Hatchery, 2)),
      new ScoutNow))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[RequestBuildable] = Seq(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(2, Zerg.Hatchery),
    Get(14, Zerg.Drone),
    Get(3, Zerg.Hatchery),
    Get(Zerg.SpawningPool),
    Get(Zerg.Extractor),
    Get(17, Zerg.Drone))

  class GoSpeedlings extends Parallel(
    new Trigger(
      new GasForUpgrade(Zerg.ZerglingSpeed),
      new CapGasAt(0)),
    new Build(Get(Zerg.ZerglingSpeed)),
    new Pump(Zerg.Zergling),
    new If(
      new And(
        new UpgradeComplete(Zerg.ZerglingSpeed),
        new MineralsAtLeast(300)),
      new Build(Get(4, Zerg.Hatchery)))
  )

  class GoHydralisks extends Parallel(

    new If(
      new UpgradeStarted(Zerg.HydraliskSpeed),
      new CapGasWorkersAt(3),
      new CapGasWorkersAt(5)),

    new Build(Get(Zerg.HydraliskDen)),
    new If(
      new UnitsAtLeast(16, Zerg.Drone),
      new BuildGasPumps(2)),

    new Pump(Zerg.Drone, 18),
    new FlipIf(
      new UnitsAtLeast(2, Zerg.Hydralisk),
      new UpgradeHydraRangeThenSpeed),
    new Pump(Zerg.Hydralisk),
    new Pump(Zerg.Drone, 20)
  )

  override def buildPlans: Seq[Plan] = Seq(

    new Write(With.blackboard.maxBuilderTravelFrames, () => GameTime(1, 0)()),

    new If(
      new And(
        new EnemiesAtLeast(1, Terran.Vulture),
        new UpgradeComplete(Zerg.ZerglingSpeed),
        new UnitsAtMost(0, Zerg.Hydralisk, complete = true)),
      new AllInIf),

    new RequireMiningBases(3),
    new Trigger(
      new Or(
        EnemyWalledIn,
        new And(
          new Not(new UpgradeStarted(Zerg.ZerglingSpeed)),
          new Or(
            new UnitsAtLeast(2, Terran.Factory),
            new EnemyStrategy(With.fingerprints.twoFacVultures)))),
      new GoHydralisks,
      new GoSpeedlings)
  )
}
