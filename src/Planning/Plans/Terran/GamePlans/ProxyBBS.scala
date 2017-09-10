package Planning.Plans.Terran.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExcept
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.{Aggression, AllIn, Attack}
import Planning.Plans.Compound.{And, If, Not, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.{FoundEnemyBase, Scout}
import Planning.ProxyPlanner
import ProxyBwapi.Races.Terran

class ProxyBBS extends Parallel {
  
  def proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  children.set(Vector(
    new Aggression(1.2),
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = proxyZone.exists( ! _.owner.isUs), placement = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Terran.Barracks), preferZone = proxyZone, respectHarvesting = proxyZone.exists( ! _.owner.isUs), placement = Some(PlacementProfiles.proxyBuilding)))
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
      new And(
        new Not(new FoundEnemyBase),
        new UnitsAtLeast(2, Terran.Barracks)),
      new Scout),
    new AllIn(new UnitsAtLeast(10, Terran.Marine, complete = true)),
    new Attack,
    new FollowBuildOrder,
    new Attack {
      attackers.get.unitCounter.set(new UnitCountExcept(8, UnitMatchWorkers))
      attackers.get.unitMatcher.set(UnitMatchWorkers)
    },
    new Gather
  ))
}