package Planning.Plans.Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Requests.{Get, RequestBuildable}
import Planning.Plan
import Planning.Plans.Army.{Aggression, AttackAndHarass, AttackWithWorkers}
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.Gameplans.All.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Placement.BuildBunkersAtEnemy
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Not
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{FoundEnemyBase, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxyBBS
import Utilities.UnitCounters.CountExcept
import Utilities.UnitFilters.IsWorker

class TvEProxyBBS extends GameplanTemplate {
  
  override val activationCriteria = new Employing(TvEProxyBBS)
  
  override def scoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutOn(Terran.Marine))
  
  override def attackPlan: Plan = new Parallel(
    new AttackAndHarass,
    new Trigger(
      new UnitsAtLeast(2, Terran.Marine),
      new AttackWithWorkers(new CountExcept(8, IsWorker))))
  
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = new If(
    new MineralsAtLeast(200),
    super.supplyPlan)
  
  override def buildOrder: Seq[RequestBuildable] = Vector(
    Get(Terran.CommandCenter),
    Get(8, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(Terran.SupplyDepot),
    Get(9, Terran.SCV),
    Get(Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Aggression(1.5),
    new Write(With.blackboard.maxBuilderTravelFrames, () => Int.MaxValue),
    new Pump(Terran.SCV, 3),
    new Pump(Terran.Marine),
    new PumpWorkers,
    new If(new UnitsAtLeast(9, Terran.SCV), new BuildBunkersAtEnemy(3)),
  )
}