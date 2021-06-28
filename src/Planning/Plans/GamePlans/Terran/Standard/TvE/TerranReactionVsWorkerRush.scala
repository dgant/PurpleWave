package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpWorkers}
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Placement.BuildBunkersAtMain
import Planning.Predicates.Compound.Or
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.EnemyStrategy
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran

class TerranReactionVsWorkerRush extends GameplanTemplate {

  override val activationCriteria: Predicate = new EnemyStrategy(With.fingerprints.workerRush)
  override val completionCriteria: Predicate = new UnitsAtLeast(2, Terran.Vulture, complete = true)

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(8, Terran.SCV),
    Get(Terran.Barracks))

  override def attackPlan: Plan = new If(new UnitsAtLeast(1, Terran.Vulture), new Attack)

  override def workerPlan: Plan = NoPlan()

  override def buildPlans: Seq[Plan] = Seq(

    new If(
      new Or(
        new UnitsAtLeast(1, Terran.Barracks, complete = true),
        new UnitsAtLeast(1, Terran.Bunker, complete = true)),
      new CapGasAt(100),
      new CapGasAt(0)),

    new If(
      new UnitsAtLeast(1, Terran.CommandCenter, complete = true),
      new CancelIncomplete(Terran.CommandCenter)),

    new Pump(Terran.Vulture),
    new Pump(Terran.SCV, 8),
    new Pump(Terran.Marine),
    new PumpWorkers(oversaturate = true),
    new BuildBunkersAtMain(1),
    new Build(
      Get(Terran.Refinery),
      Get(2, Terran.Factory))
  )
}
