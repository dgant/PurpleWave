package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.{Do, NoPlan, Write}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.FoundEnemyBase
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, Predicate, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxy5Rax

class TvEProxy5Rax extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvEProxy5Rax)

  override def placementPlan: Plan = new ProposePlacement{
    override lazy val blueprints = Vector(
      new Blueprint(this,
        building = Some(Terran.Barracks),
        preferZone = ProxyPlanner.proxyMiddle,
        placement = Some(PlacementProfiles.proxyBuilding)))
  }

  override def initialScoutPlan: Plan = new If(
    new StartPositionsAtLeast(3),
    new If(
      new Not(new FoundEnemyBase),
      new If(
        new StartPositionsAtLeast(4),
        new ScoutAt(10, 2),
        new ScoutAt(10))))

  override def aggressionPlan = new Aggression(1.5)
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Parallel(new Attack, new Attack(UnitMatchWorkers, new UnitCountExcept(3, UnitMatchWorkers)))

  override val buildOrder = Vector(
    Get(5, Terran.SCV),
    Get(Terran.Barracks),
    Get(7, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(Terran.Marine)
  )

  override def buildPlans: Seq[Plan] = Vector(
    new Write(With.blackboard.pushKiters, true),
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Pump(Terran.Marine),
    /*
    new If(
      new UnitsAtLeast(1, Terran.Marine),
      new RecruitFreelancers(UnitMatchWorkers, new UnitCountExcept(3, UnitMatchWorkers))),
    */
  )
}