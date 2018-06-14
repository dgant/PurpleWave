package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, GetAtLeast}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Economy.{GasAtLeast, SupplyBlocked}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEProxy8Fact

class Proxy8Fact extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEProxy8Fact)
  
  override val aggression = 1.5
  
  override def defaultAttackPlan: Plan = new Attack
  
  override def defaultScoutPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Terran.Factory, complete = true),
    new Scout)
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  override def defaultPlacementPlan: Plan = new ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(this, building = Some(Terran.Factory),  preferZone = proxyZone, respectHarvesting = false, placement = Some(PlacementProfiles.proxyBuilding)))
  }
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    GetAtLeast(1, Terran.CommandCenter),
    GetAtLeast(8, Terran.SCV),
    GetAtLeast(1, Terran.Barracks),
    GetAtLeast(1, Terran.Refinery),
    GetAtLeast(1, Terran.Factory),
    GetAtLeast(1, Terran.SupplyDepot),
    GetAtLeast(10, Terran.SCV))
  
  override def defaultWorkerPlan: Plan = NoPlan()
  
  override def defaultSupplyPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Terran.Factory, complete = true),
    initialAfter = super.defaultSupplyPlan)
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new If(
      new And(
        new Or(
          new GasAtLeast(100),
          new UnitsAtLeast(1, Terran.Factory))),
      new Do(() => {
        With.blackboard.gasTargetRatio = 0
        With.blackboard.gasLimitFloor = 0
        With.blackboard.gasLimitCeiling = 0
      }),
      new Do(() => {
        With.blackboard.gasTargetRatio = 1.0
        With.blackboard.gasLimitFloor = 100
        With.blackboard.gasLimitCeiling = 100
      })),
    new Trigger(
      new UnitsAtLeast(1, Terran.Factory, complete = false),
      initialAfter = new Parallel(
        new If(
          new SupplyBlocked,
          new RequireSufficientSupply),
        new TrainContinuously(Terran.Wraith),
        new TrainContinuously(Terran.Vulture),
        new TrainContinuously(Terran.Marine),
        new TrainWorkersContinuously,
        new Build(
          GetAtLeast(1, Terran.Starport),
          GetAtLeast(3, Terran.Barracks))))
  )
}