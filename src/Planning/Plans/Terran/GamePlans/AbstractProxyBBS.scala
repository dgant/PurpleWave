package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWorkers}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plans.Army.{Aggression, AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Terran

abstract class AbstractProxyBBS extends Parallel {
  
  protected def proxyZone: Option[Zone]
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  children.set(Vector(
    new Aggression(3.5),
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)))
    },
    new RequireMiningBases(1),
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(8, Terran.SCV),
        RequestAtLeast(2, Terran.Barracks),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(1, Terran.Marine))),
    new RequireSufficientSupply,
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.SCV),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Terran.Barracks), complete = true),
      new Scout),
    new AllIn(new UnitsAtLeast(10, UnitMatchType(Terran.Marine), complete = true)),
    new Attack,
    new FollowBuildOrder,
    new Gather { workers.unitCounter.set(new UnitCountBetween(0, 8)); workers.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter)) },
    new Attack { attackers.get.unitMatcher.set(UnitMatchWorkers) }
  ))
}