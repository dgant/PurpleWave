package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWorkers}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Terran

class Proxy5Rax extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomatic
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  children.set(Vector(
    new ProposePlacement{
      override lazy val blueprints = Vector(new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)))
    },
    new Aggression(1.5),
    new RequireMiningBases(1),
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
      new UnitsAtLeast(1, UnitMatchType(Terran.Barracks), complete = true),
      initialWhenTrue = new Parallel(
        new Scout,
        new Attack,
        new Gather { workers.unitCounter.set(new UnitCountBetween(0, 4)); workers.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter)) },
        new Attack { attackers.get.unitMatcher.set(UnitMatchWorkers) }
      ),
      initialWhenFalse = new Parallel(
        new Gather
      )
    )
  ))
}