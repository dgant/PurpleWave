package Planning.Plans.GamePlans.Terran.TvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army.{Aggression, AttackAndHarass, AttackWithWorkers}
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.FoundEnemyBase
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvTProxy5Rax
import Utilities.UnitCounters.CountExcept
import Utilities.UnitFilters.IsWorker

class TvTProxy5Rax extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvTProxy5Rax)

  override def scoutPlan: Plan = new If(
    new StartPositionsAtLeast(3),
    new If(
      new Not(new FoundEnemyBase),
      new If(
        new StartPositionsAtLeast(4),
        new ScoutAt(10, 2),
        new ScoutAt(10))))

  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Parallel(new AttackAndHarass, new AttackWithWorkers(new CountExcept(3, IsWorker)))

  override val buildOrder = Vector(
    Get(5, Terran.SCV),
    Get(Terran.Barracks),
    Get(7, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(Terran.Marine)
  )

  override def buildPlans: Seq[Plan] = Vector(
    new Aggression(1.5),
    new Write(With.blackboard.pushKiters, () => true),
    new Write(With.blackboard.maxBuilderTravelFrames, () => Int.MaxValue),
    new Pump(Terran.Marine),
  )
}