package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, AllIn, Attack, RecruitFreelancers}
import Planning.Plans.Basic.{Do, NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Placement.{BuildBunkersAtEnemy, ProposePlacement}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UnitsAtMost}
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
        new Blueprint(
          Terran.Barracks,
          preferZone = ProxyPlanner.proxyMiddle,
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

  override def aggressionPlan = new Aggression(1.5)
  override def workerPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Parallel(new Attack, new Attack(Terran.SCV))

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency,
    new TerranReactionVsWorkerRush
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
    new AllIn(
      new And(
        new EnemiesAtLeast(1, Terran.Vulture),
        new UnitsAtMost(0, Terran.Bunker))),
    new Write(With.blackboard.pushKiters, true),
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new DefendFightersAgainstEarlyPool,
    new Pump(Terran.Marine),
    new If(
      new FoundEnemyBase,
      new BuildBunkersAtEnemy(1)),
    new If(
      new UnitsAtLeast(1, Terran.Marine, complete = true),
      new RecruitFreelancers(UnitMatchWorkers, new UnitCountExcept(4, UnitMatchWorkers))),
    new If(
      new MineralsAtLeast(150),
      new Pump(Terran.SCV))
  )
}
