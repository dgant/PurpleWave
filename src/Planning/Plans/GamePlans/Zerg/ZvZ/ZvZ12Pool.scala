package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.Requests.{RequestBuildable, Get}
import Planning.Plans.Army.{AllInIf, AttackAndHarass, ConsiderAttacking}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Zerg.ZergIdeas.PumpJustEnoughScourge
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases}
import Planning.Plans.Placement.BuildSunkensAtNatural
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ12Pool

class ZvZ12Pool extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ12Pool)

  override def scoutPlan: Plan = NoPlan()

  override def attackPlan: Plan = new Parallel(
    new If(new UnitsAtLeast(1, Zerg.Mutalisk, complete = true), new AttackAndHarass),
    new If(
      new EnemyHasShown(Zerg.Mutalisk),
      new AttackAndHarass),
    new If(
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new ConsiderAttacking))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[RequestBuildable] = Vector(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(13, Zerg.Drone),
    Get(Zerg.Extractor))

  override def buildPlans: Seq[Plan] = Vector(

    new AllInIf(
      new And(
        new UnitsAtMost(0, Zerg.Spire, complete = true),
        new EnemiesAtLeast(1, Zerg.Mutalisk))),

    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new CapGasAtRatioToMinerals(1.0, 100),
      new Parallel(
        new CapGasAt(150),
        new CapGasWorkersAt(2))),

    new Pump(Zerg.SunkenColony),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.fourPool)),
      new RequireBases(2)),

    new BuildOrder(
      Get(8, Zerg.Zergling),
      Get(Zerg.ZerglingSpeed)),

    new If(
      new UnitsAtLeast(1, Zerg.Mutalisk),
      new PumpJustEnoughScourge,
      new PumpRatio(Zerg.Scourge, 0, 12, Seq(Enemy(Zerg.Mutalisk, 2.0)))),
    new Pump(Zerg.Mutalisk, 5),
    new If(
      new Or(
        new UnitsAtLeast(14, Zerg.Drone),
        new And(
          new UnitsAtLeast(7, Zerg.Drone),
          new EnemyHasShown(Zerg.Mutalisk))),
      new BuildGasPumps),

    new Pump(Zerg.Zergling, 10),
    new Pump(Zerg.Drone, 6),
    new If(
      new Not(new EnemyStrategy(With.fingerprints.twelvePool)),
      new BuildSunkensAtNatural(1)),

    new FlipIf(

      new Or(
        new SafeAtHome,
        new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.twelvePool)),

      new Pump(Zerg.Zergling),

      new If(
        new Or(
          new UnitsAtLeast(8, Zerg.Drone),
          new EnemyHasShown(Zerg.Mutalisk)),
        new Build(
          Get(Zerg.Lair),
          Get(Zerg.Spire)))),
  )
}
