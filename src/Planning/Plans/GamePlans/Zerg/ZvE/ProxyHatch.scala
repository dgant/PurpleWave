package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchMobileFlying, UnitMatchWorkers}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Employ
import Planning.Plans.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.ScoutAt
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.{ProxyHatchHydras, ProxyHatchSunkens, ProxyHatchZerglings}

class ProxyHatch extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  private class WeKnowWhereToProxy extends Check(() => ProxyPlanner.proxyEnemyNatural.isDefined)
  private class WeHaveEnoughSunkens extends UnitsAtLeast(3, Zerg.SunkenColony, complete = false)
  
  private def blueprintCreepColonyNatural: Blueprint = new Blueprint(this,
    building     = Some(Zerg.CreepColony),
    requireZone  = if (With.enemy.isTerran) ProxyPlanner.proxyEnemyNatural else ProxyPlanner.proxyMiddle,
    placement    = Some(PlacementProfiles.proxyCannon))
  
  children.set(Vector(
    new Plan { override def onUpdate(): Unit = {
      With.blackboard.gasLimitFloor = if (With.units.existsOurs(Zerg.Lair)) 400 else 100
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
          new Blueprint(this, preferZone = ProxyPlanner.proxyEnemyNatural, building = Some(Zerg.Hatchery), placement = Some(PlacementProfiles.proxyBuilding)),
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
        new Trigger(
          new WeKnowWhereToProxy,
          new TrainContinuously(Zerg.Hatchery, 5, 1)))),
  
    new Employ(ProxyHatchHydras,
      new Parallel(
        new Do(() => {
          With.blackboard.gasTargetRatio = 2.0 / 12.0
          With.blackboard.gasLimitFloor = 50
          With.blackboard.gasLimitCeiling = 175
        }),
        new Build(RequestAtLeast(1, Zerg.HydraliskDen)),
        new RequireSufficientSupply,
        new If(
          new UnitsAtLeast(1, Zerg.HydraliskDen, complete = false),
          new TrainContinuously(Zerg.Hydralisk),
          new TrainContinuously(Zerg.Zergling)),
        new UpgradeContinuously(Zerg.HydraliskSpeed),
        new UpgradeContinuously(Zerg.HydraliskRange))),
  
    new Employ(ProxyHatchSunkens,
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new Trigger(
          new UnitsAtLeast(2, Zerg.Hatchery, complete = true),
          initialAfter = new Trigger(
            new WeHaveEnoughSunkens,
            initialBefore = new Parallel(
              new TrainContinuously(Zerg.CreepColony, 2),
              new TrainContinuously(Zerg.Zergling)),
            initialAfter = new Parallel(
              new RequireSufficientSupply,
              new TrainContinuously(Zerg.Mutalisk),
              new Build(RequestAtLeast(24, Zerg.Drone)),
              new BuildGasPumps,
              new TrainContinuously(Zerg.Zergling),
              new Build(
                RequestAtLeast(1, Zerg.Lair),
                RequestUpgrade(Zerg.ZerglingSpeed),
                RequestAtLeast(1, Zerg.Spire)),
              new If(
                new UnitsAtMost(6, Zerg.SunkenColony),
                new TrainContinuously(Zerg.CreepColony, 1)),
              new TrainContinuously(Zerg.Hatchery, 8)
            ))))),
      
    new If(new Not(new WeKnowWhereToProxy), new ScoutAt(8, 2)),
    
    new Aggression(1.5),
    new Attack,
    new FollowBuildOrder,
  
    new Employ(ProxyHatchSunkens,
      new If(
        new And(
          new UnitsAtLeast(2, Zerg.Hatchery,      complete = false),
          new UnitsAtLeast(1, Zerg.SpawningPool,  complete = false),
          new Not(new WeHaveEnoughSunkens)),
        new Attack(UnitMatchWorkers, UnitCountExactly(2)),
        new Attack(UnitMatchMobileFlying))),
    
    new Gather
  ))
}
