package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
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
      new Attack {
        attackers.get.unitCounter.set(UnitCountExactly(2))
        attackers.get.unitMatcher.set(UnitMatchWorkers)
      })
  )
  
  override def defaultWorkerPlan: Plan = NoPlan()
  override def defaultSupplyPlan: Plan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(1, Terran.CommandCenter),
    RequestAtLeast(8, Terran.SCV),
    RequestAtLeast(2, Terran.Barracks),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
    },
    new TrainContinuously(Terran.Marine),
    new Build(RequestAtLeast(10, Terran.SCV)),
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
        new TrainContinuously(Terran.Bunker, 3, 1)
      ))
  )
}