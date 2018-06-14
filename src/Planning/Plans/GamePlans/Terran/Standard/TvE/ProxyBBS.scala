package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxyBBS

class ProxyBBS extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEProxyBBS)
  
  override def aggression: Double = 1.5
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticAggressive
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack,
    new Trigger(
      new UnitsAtLeast(1, Terran.Marine, complete = false),
      new Attack(UnitMatchWorkers, UnitCountExactly(2))))
  
  override def defaultWorkerPlan: Plan = NoPlan()
  override def defaultSupplyPlan: Plan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(1, Terran.CommandCenter),
    Get(8, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(1, Terran.SupplyDepot),
    Get(9, Terran.SCV),
    Get(1, Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
    },
    new Pump(Terran.Marine),
    new Build(Get(10, Terran.SCV)),
    new If(
      new And(
        new UnitsAtLeast(9, Terran.SCV),
        new EnemyBasesAtLeast(1)),
      new Parallel(
        new ProposePlacement {
          override lazy val blueprints = Vector(
            new Blueprint(this, building = Some(Terran.Bunker), preferZone = With.geography.enemyBases.headOption.map(_.zone), respectHarvesting = false),
            new Blueprint(this, building = Some(Terran.Bunker), preferZone = With.geography.enemyBases.headOption.map(_.zone), respectHarvesting = false),
            new Blueprint(this, building = Some(Terran.Bunker), preferZone = With.geography.enemyBases.headOption.map(_.zone), respectHarvesting = false),
            new Blueprint(this, building = Some(Terran.Bunker), preferZone = With.geography.enemyBases.headOption.map(_.zone), respectHarvesting = false)
          )
        },
        new Pump(Terran.Bunker, 3, 1)
      ))
  )
}