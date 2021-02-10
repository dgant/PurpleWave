package Planning.Plans.GamePlans.Terran.Standard.TvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, AttackWithWorkers}
import Planning.Plans.Basic.{Do, NoPlan, Write}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Placement.ProposePlacement
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.Not
import Planning.Predicates.Milestones.FoundEnemyBase
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.MatchWorkers
import Planning.{Plan, Predicate, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvTProxy5Rax

class TvTProxy5Rax extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvTProxy5Rax)

  override def placementPlan: Plan = new ProposePlacement{
    override lazy val blueprints = Vector(
      new Blueprint(
        Terran.Barracks,
        preferZone = ProxyPlanner.proxyMiddle,
        placement = Some(PlacementProfiles.proxyBuilding)))
  }

  override def scoutPlan: Plan = new If(
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
  override def attackPlan: Plan = new Parallel(new Attack, new AttackWithWorkers(new UnitCountExcept(3, MatchWorkers)))

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
  )
}