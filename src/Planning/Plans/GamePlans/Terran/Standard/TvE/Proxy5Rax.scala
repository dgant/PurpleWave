package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExcept
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxy5Rax

class Proxy5Rax extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEProxy5Rax)
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticAggressive
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  children.set(Vector(
    new ProposePlacement{
      override lazy val blueprints = Vector(new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
    },
    new Aggression(1.5),
    new RequireEssentials,
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(5, Terran.SCV),
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(7, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(1, Terran.Marine))),
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.SCV),
    new FollowBuildOrder,
    new If(
      new UnitsAtLeast(1, Terran.Barracks, complete = true),
      initialWhenTrue = new Parallel(
        new Scout,
        new Attack,
        new Attack {
          attackers.get.unitCounter.set(new UnitCountExcept(4, UnitMatchWorkers))
          attackers.get.unitMatcher.set(UnitMatchWorkers)
        })
    ),
    new Gather
  ))
}