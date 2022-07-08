package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Cancel
import Planning.Plans.Placement.BuildSunkensInMain
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Milestones.{UnitsAtLeast, UpgradeStarted}
import Planning.Predicates.Strategy.EnemyStrategy
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Zerg

class ZergReactionVsWorkerRush extends GameplanTemplate {

  override val activationCriteria: Predicate = new EnemyStrategy(With.fingerprints.workerRush)
  override val completionCriteria: Predicate = new And(new UnitsAtLeast(6, Zerg.Zergling, complete = true), new UpgradeStarted(Zerg.ZerglingSpeed))

  override def attackPlan: Plan = new AttackAndHarass

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new And(activationCriteria, new Not(completionCriteria)),
      new Parallel(
        new If(
          new And(
            new UnitsAtLeast(2, Zerg.Zergling),
            new UnitsAtLeast(1, Zerg.SpawningPool)),
          new CapGasAt(100),
          new CapGasAt(0)),

        new If(
          new UnitsAtLeast(1, Zerg.Hatchery, complete = true),
          new Cancel(Zerg.Hatchery)),

        new Pump(Zerg.SunkenColony),
        new Pump(Zerg.Drone, 6),
        new Pump(Zerg.Zergling, 6),
        new BuildOrder(
          Get(6, Zerg.Drone),
          Get(Zerg.SpawningPool),
          Get(8, Zerg.Drone)),
        new If(
          new UnitsAtLeast(1, Zerg.SpawningPool),
          new BuildSunkensInMain(1)),
        new Pump(Zerg.Drone, 9),
        new If(
          new Or(
            new UnitsAtLeast(1, Zerg.SunkenColony, complete = true),
            new UnitsAtLeast(6, Zerg.Zergling, complete = true)),
          new Build(
            Get(Zerg.Extractor),
            Get(Zerg.ZerglingSpeed))),
        new Pump(Zerg.Zergling))))
}
