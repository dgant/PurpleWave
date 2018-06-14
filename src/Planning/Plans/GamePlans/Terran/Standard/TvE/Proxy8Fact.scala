package Planning.Plans.GamePlans.Terran.Standard.TvE

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Predicates.Compound.And
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Economy.{GasAtLeast, SupplyBlocked}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Strategy.Employing
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
    Get(1, Terran.CommandCenter),
    Get(8, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(1, Terran.Refinery),
    Get(1, Terran.Factory),
    Get(1, Terran.SupplyDepot),
    Get(10, Terran.SCV))
  
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
        new Pump(Terran.Wraith),
        new Pump(Terran.Vulture),
        new Pump(Terran.Marine),
        new PumpWorkers,
        new Build(
          Get(1, Terran.Starport),
          Get(3, Terran.Barracks))))
  )
}