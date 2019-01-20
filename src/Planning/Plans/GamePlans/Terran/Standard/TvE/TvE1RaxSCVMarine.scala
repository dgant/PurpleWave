package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, RecruitFreelancers}
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Terran.BuildBunkersAtEnemy
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.Predicates.Compound.{Check, Not}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, Predicate, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.MapGroups
import Strategery.Strategies.Terran.TvE.TvE1RaxSCVMarine

class TvE1RaxSCVMarine extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvE1RaxSCVMarine)

  override def placementPlan: Plan = new If(
    new Check(() => ! MapGroups.badForProxying.exists(_.matches)),
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this,
          building = Some(Terran.Barracks),
          preferZone = ProxyPlanner.proxyMiddleBase,
          respectHarvesting = Some(false),
          placement = Some(PlacementProfiles.proxyBuilding)))
    })

  override def scoutPlan: Plan = new If(
    new StartPositionsAtLeast(3),
    new If(
      new Not(new FoundEnemyBase),
      new If(
        new StartPositionsAtLeast(4),
        new ScoutAt(10, 2),
        new ScoutAt(10))))

  override val aggression: Double = 1.5
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Parallel(new Attack, new Attack(Terran.SCV))

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency
  )
  
  override val buildOrder = Vector(
    Get(8, Terran.SCV),
    Get(Terran.Barracks),
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(10, Terran.SCV),
    Get(Terran.Marine),
    Get(11, Terran.SCV))
  
  override def buildPlans: Seq[Plan] = Vector(
    new DefendFightersAgainstEarlyPool,
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Pump(Terran.Marine),
    new BuildBunkersAtEnemy(1),
    new Build(Get(1, Terran.Bunker)), // Workaround for build limitations
    new If(
      new UnitsAtLeast(1, Terran.Marine, complete = true),
      new RecruitFreelancers(UnitMatchWorkers, new UnitCountExcept(4, UnitMatchWorkers))),
  )
}
