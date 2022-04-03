package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.Requests.{RequestProduction, Get}
import Planning.Plans.Army.{Aggression, AttackAndHarass, AttackWithWorkers}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Placement.{BuildBunkersAtEnemy, ProposePlacement}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Not
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{FoundEnemyBase, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import Planning.UnitCounters.CountExcept
import Planning.UnitMatchers.MatchWorker
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxyBBS

class TvEProxyBBS extends GameplanTemplate {
  
  override val activationCriteria = new Employing(TvEProxyBBS)

  override def aggressionPlan: Plan = new Aggression(1.5)
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyMiddle
  
  override def scoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutOn(Terran.Marine))

  override def placementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(Terran.Barracks, preferZone = proxyZone, respectHarvesting = Some(false), placement = Some(PlacementProfiles.proxyBuilding)),
      new Blueprint(Terran.Barracks, preferZone = proxyZone, respectHarvesting = Some(false), placement = Some(PlacementProfiles.proxyBuilding)))
  }
  
  override def attackPlan: Plan = new Parallel(
    new AttackAndHarass,
    new Trigger(
      new UnitsAtLeast(2, Terran.Marine),
      new AttackWithWorkers(new CountExcept(8, MatchWorker))))
  
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = new If(
    new MineralsAtLeast(200),
    super.supplyPlan)
  
  override def buildOrder: Seq[RequestProduction] = Vector(
    Get(Terran.CommandCenter),
    Get(8, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(Terran.SupplyDepot),
    Get(9, Terran.SCV),
    Get(Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Pump(Terran.SCV, 3),
    new Pump(Terran.Marine),
    new PumpWorkers,
    new If(new UnitsAtLeast(9, Terran.SCV), new BuildBunkersAtEnemy(3)),
  )
}