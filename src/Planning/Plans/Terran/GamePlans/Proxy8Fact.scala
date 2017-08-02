package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Terran

class Proxy8Fact extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomatic
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  children.set(Vector(
    new ProposePlacement {
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Factory),  preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Starport), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Starport), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)))
    },
    new RequireMiningBases(1),
    new Build(
      RequestAtLeast(1, Terran.CommandCenter),
      RequestAtLeast(8, Terran.SCV),
      RequestAtLeast(1, Terran.Barracks),
      RequestAtLeast(1, Terran.Refinery)),
    new Trigger(
      new UnitsAtLeast(1, UnitMatchType(Terran.Barracks), complete = true),
      initialAfter =
        new Parallel(
          new Build(
            RequestAtLeast(1, Terran.Factory),
            RequestAtLeast(1, Terran.SupplyDepot),
            RequestAtLeast(10, Terran.SCV)),
          new If(
            new Check(() => With.self.supplyUsed >= With.self.supplyTotal),
            new RequireSufficientSupply),
          new TrainContinuously(Terran.Wraith),
          new TrainContinuously(Terran.Vulture),
          new TrainContinuously(Terran.Marine),
          new TrainWorkersContinuously,
          new TrainContinuously(Terran.Starport, 2))),
    new Trigger(
      new UnitsAtLeast(1, UnitMatchType(Terran.Factory), complete = true),
      initialAfter = new Scout),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}