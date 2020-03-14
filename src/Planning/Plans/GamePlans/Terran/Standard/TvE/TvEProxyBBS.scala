package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Terran.BuildBunkersAtEnemy
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutOn}
import Planning.Predicates.Compound.Not
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxyBBS

class TvEProxyBBS extends GameplanTemplate {
  
  override val activationCriteria = new Employing(TvEProxyBBS)

  override def aggressionPlan: Plan = new Aggression(1.5)
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyMiddle
  
  override def scoutWorkerPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutOn(Terran.Marine))

  override def placementPlan: Plan = new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = Some(false), placement = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = Some(false), placement = Some(PlacementProfiles.proxyBuilding)))
    }

  object AttackWithWorkersCondition extends UnitsAtLeast(2, Terran.Marine)
  object CountWorkerAttackers extends UnitCountExcept(8, UnitMatchWorkers)
  
  override def attackPlan: Plan = new Parallel(
    new Attack,
    new Trigger(
      AttackWithWorkersCondition,
      new Attack(UnitMatchWorkers, CountWorkerAttackers)))
  
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = new If(
    new MineralsAtLeast(200),
    super.supplyPlan)
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(Terran.CommandCenter),
    Get(8, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(Terran.SupplyDepot),
    Get(9, Terran.SCV),
    Get(Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Pump(Terran.Marine),
    new Build(Get(10, Terran.SCV)),
    new If(
      new UnitsAtLeast(9, Terran.SCV),
      new BuildBunkersAtEnemy(3)),

    // Reenable once we stop the conscription workaround for attack squads
    // new Trigger(AttackWithWorkersCondition, new RecruitFreelancers(UnitMatchWorkers, CountWorkerAttackers))
  )
}