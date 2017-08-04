package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchMobileFlying, UnitMatchType, UnitMatchWorkers}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.ScoutAt
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.Global.{ProxyHatchHydras, ProxyHatchSunkens, ProxyHatchZerglings}

class ProxyHatch extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  private class WeKnowWhereToProxy extends Check(() => ProxyPlanner.proxyEnemyNatural.isDefined)
  private class WeHaveEnoughSunkens extends UnitsAtLeast(6, UnitMatchType(Zerg.SunkenColony), complete = false)
  
  private def blueprintCreepColonyNatural: Blueprint = new Blueprint(this,
    building          = Some(Zerg.CreepColony),
    requireZone       = ProxyPlanner.proxyEnemyNatural,
    placementProfile  = Some(PlacementProfiles.proxyCannon))
  
  children.set(Vector(
    new Plan { override def onUpdate(): Unit = {
      With.blackboard.gasBankSoftLimit = if (With.units.ours.exists(_.is(Zerg.Lair))) 400 else 100
      With.blackboard.gasBankHardLimit = With.blackboard.gasBankSoftLimit
    }},
    new ProposePlacement { override lazy val blueprints = Vector(new Blueprint(this,
      building = Some(Zerg.Extractor),
      requireZone = Some(With.geography.ourMain.zone))) },
    
    new Build(
      RequestAtLeast(1,   Zerg.Hatchery),
      RequestAtLeast(1,   Zerg.Drone),
      RequestAtLeast(1,   Zerg.Overlord),
      RequestAtLeast(9,   Zerg.Drone)),
  
    new If(
      new WeKnowWhereToProxy,
      new Parallel(
        new ProposePlacement { override lazy val blueprints = Vector(
          new Blueprint(this, preferZone = ProxyPlanner.proxyEnemyNatural, building = Some(Zerg.Hatchery), placementProfile = Some(PlacementProfiles.proxyBuilding)),
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural,
          blueprintCreepColonyNatural)},
        new Employ(ProxyHatchHydras,
          new Build(
            RequestAtLeast(2,   Zerg.Hatchery),
            RequestAtLeast(1,   Zerg.SpawningPool),
            RequestAtLeast(2,   Zerg.Overlord),
            RequestAtLeast(1,   Zerg.Extractor),
            RequestAtLeast(12,  Zerg.Drone))),
        new Employ(ProxyHatchZerglings,
          new Build(
            RequestAtLeast(2,   Zerg.Hatchery),
            RequestAtLeast(1,   Zerg.SpawningPool),
            RequestAtLeast(2,   Zerg.Overlord))),
        new Employ(ProxyHatchSunkens,
          new Build(
            RequestAtLeast(2,   Zerg.Overlord),
            RequestAtLeast(2,   Zerg.Hatchery),
            RequestAtLeast(12,  Zerg.Drone),
            RequestAtLeast(1,   Zerg.SpawningPool),
            RequestAtLeast(14,  Zerg.Drone))))),
  
    new Employ(ProxyHatchZerglings,
      new Parallel(
        new AddSupplyWhenSupplyBlocked,
        new TrainContinuously(Zerg.Zergling),
        new If(
          new And(
            new WeKnowWhereToProxy,
            new Check(() => ! With.units.ours.exists(_.is(Zerg.Larva)))),
          new TrainContinuously(Zerg.Hatchery)))),
  
    new Employ(ProxyHatchHydras,
      new Parallel(
        new Build(RequestAtLeast(1, Zerg.HydraliskDen)),
        new RequireSufficientSupply,
        new If(
          new UnitsAtLeast(1, UnitMatchType(Zerg.HydraliskDen), complete = false),
          new TrainContinuously(Zerg.Hydralisk),
          new TrainContinuously(Zerg.Zergling)))),
  
    new Employ(ProxyHatchSunkens,
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new Trigger(
          new UnitsAtLeast(2, UnitMatchType(Zerg.Hatchery), complete = true),
          initialAfter = new Trigger(
            new WeHaveEnoughSunkens,
            initialBefore = new Parallel(
              new TrainContinuously(Zerg.Zergling),
              new TrainContinuously(Zerg.CreepColony, 2)),
            initialAfter = new Parallel(
              new RequireSufficientSupply,
              new TrainContinuously(Zerg.Mutalisk),
              new TrainWorkersContinuously,
              new BuildGasPumps,
              new Build(RequestAtLeast(1, Zerg.Lair)),
              new Build(RequestAtLeast(1, Zerg.Spire)),
              new TrainContinuously(Zerg.CreepColony, 1),
              new TrainContinuously(Zerg.Hatchery, 8)
            ))))),
      
    new If(new Not(new WeKnowWhereToProxy), new ScoutAt(8, 2)),
    
    new Aggression(2.5),
    new Attack,
    new FollowBuildOrder,
  
    new Employ(ProxyHatchSunkens,
      new If(
        new And(
          new UnitsAtLeast(2, UnitMatchType(Zerg.Hatchery),     complete = false),
          new UnitsAtLeast(1, UnitMatchType(Zerg.SpawningPool), complete = false),
          new Not(new WeHaveEnoughSunkens)),
        new Attack {
          attackers.get.unitMatcher.set(UnitMatchWorkers)
          attackers.get.unitCounter.set(UnitCountExactly(2))
        },
        new Attack { attackers.get.unitMatcher.set(UnitMatchMobileFlying) })),
    
    new Gather
  ))
}
