package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{AllInIf, Attack}
import Planning.Plans.Basic.{Do, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.UpgradeHydraRangeThenSpeed
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.ScoutWithWorkers
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT3HatchLing
import Utilities.GameTime

class ZvT3HatchLing extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT3HatchLing)

  override def attackPlan: Plan = new Parallel(
    new If(new UpgradeComplete(Zerg.HydraliskRange), new Attack),
    new If(
      new Or(
        new EnemiesAtMost(0, Terran.Factory),
        new UpgradeComplete(Zerg.ZerglingSpeed)),
      new Attack))

  override def initialScoutPlan: Plan = new If(
    new And(
      new Not(new EnemyWalledIn),
      new Not(new EnemyStrategy(With.fingerprints.twoFacVultures))),
    new Trigger(
      new Or(
        new And(
          new StartPositionsAtLeast(4),
          new MineralsForUnit(Zerg.Overlord, 2)),
        new MineralsForUnit(Zerg.Hatchery, 2)),
      new ScoutWithWorkers))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[BuildRequest] = Seq(
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
      new UnitsAtLeast(16, Zerg.Drone, countEggs = true),
      new BuildGasPumps(2)),

    new Pump(Zerg.Drone, 18),
    new FlipIf(
      new UnitsAtLeast(2, Zerg.Hydralisk, countEggs = true),
      new UpgradeHydraRangeThenSpeed),
    new Pump(Zerg.Hydralisk),
    new Pump(Zerg.Drone, 20)
  )

  override def buildPlans: Seq[Plan] = Seq(

    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = GameTime(1, 0)()),
    new Write(With.blackboard.preferCloseExpansion, true),

    new If(
      new And(
        new EnemiesAtLeast(1, Terran.Vulture),
        new UpgradeComplete(Zerg.ZerglingSpeed),
        new UnitsAtMost(0, Zerg.Hydralisk, complete = true)),
      new AllInIf),

    new RequireMiningBases(3),
    new Trigger(
      new Or(
        new EnemyWalledIn,
        new And(
          new Not(new UpgradeStarted(Zerg.ZerglingSpeed)),
          new Or(
            new UnitsAtLeast(2, Terran.Factory),
            new EnemyStrategy(With.fingerprints.twoFacVultures)))),
      new GoHydralisks,
      new GoSpeedlings)
  )
}
