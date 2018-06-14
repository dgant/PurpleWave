package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Predicates.Employing
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxy5Rax

class Proxy5Rax extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEProxy5Rax)
  
  override def aggression: Double = 1.5
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticAggressive
  
  override def defaultScoutPlan: Plan = NoPlan()
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack,
    new Trigger(
      new UnitsAtLeast(1, Terran.Marine, complete = false),
      new Attack(UnitMatchWorkers, new UnitCountExcept(3, UnitMatchWorkers))))
  
  override def defaultWorkerPlan: Plan = NoPlan()
  override def defaultSupplyPlan: Plan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(1, Terran.CommandCenter),
    Get(5, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(7, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(1, Terran.Marine)
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new ProposePlacement{
      override lazy val blueprints = Vector(new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
    },
    new Pump(Terran.Marine)
  )
}