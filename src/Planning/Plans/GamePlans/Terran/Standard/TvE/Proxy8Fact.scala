package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxy8Fact

class Proxy8Fact extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEProxy8Fact)
  
  override def defaultScoutPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Terran.Factory, complete = true),
    new Scout)
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  
  override def defaultPlacementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
      new Blueprint(this, building = Some(Terran.Factory),  preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
      new Blueprint(this, building = Some(Terran.Starport), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)),
      new Blueprint(this, building = Some(Terran.Starport), preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
  }
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(1, Terran.CommandCenter),
    RequestAtLeast(8, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(1, Terran.Refinery))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(
      new Check(() => With.self.gas >= 100),
      new Do(() => {
        With.blackboard.gasTargetRatio = 0
        With.blackboard.gasLimitFloor = 0
        With.blackboard.gasLimitCeiling = 0
      })),
    new Trigger(
      new UnitsAtLeast(1, Terran.Barracks, complete = true),
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
          new TrainContinuously(Terran.Starport, 2)))
  )
}