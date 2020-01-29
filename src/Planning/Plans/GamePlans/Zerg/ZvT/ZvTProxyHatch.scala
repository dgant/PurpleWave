package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.Employing
import Planning.ProxyPlanner
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.{UnitMatchMobileFlying, UnitMatchWorkers}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.{ZvTProxyHatchHydras, ZvTProxyHatchSunkens, ZvTProxyHatchZerglings}

class ZvTProxyHatch extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  private class WeKnowWhereToProxy extends Check(() => ProxyPlanner.proxyEnemyNatural.isDefined)
  private class WeHaveEnoughSunkens extends UnitsAtLeast(3, Zerg.SunkenColony, complete = false)
  
  private def blueprintCreepColonyNatural: Blueprint = new Blueprint(this,
    building     = Some(Zerg.CreepColony),
    requireZone  = ProxyPlanner.proxyEnemyNatural,
    placement    = Some(PlacementProfiles.proxyCannon))
  
  children.set(Vector(
    new ZergReactionVsWorkerRush,

    new If(
      new UnitsAtLeast(1, Zerg.Lair),
      new CapGasAt(400),
      new CapGasAt(100)),
    new ProposePlacement { override lazy val blueprints = Vector(new Blueprint(this,
      building = Some(Zerg.Extractor),
      requireZone = Some(With.geography.ourMain.zone))) },
    
    new Build(
      Get(Zerg.Hatchery),
      Get(Zerg.Drone),
      Get(Zerg.Overlord),
      Get(9, Zerg.Drone)),
  
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
        new If(
          new Employing(ZvTProxyHatchHydras),
          new Build(
            Get(2, Zerg.Hatchery),
            Get(Zerg.SpawningPool),
            Get(2, Zerg.Overlord),
            Get(Zerg.Extractor),
            Get(12, Zerg.Drone))),
        new If(
          new Employing(ZvTProxyHatchZerglings),
          new Build(
            Get(2, Zerg.Hatchery),
            Get(Zerg.SpawningPool),
            Get(2,  Zerg.Overlord))),
        new If(
          new Employing(ZvTProxyHatchSunkens),
          new Build(
            Get(2, Zerg.Overlord),
            Get(2, Zerg.Hatchery),
            Get(12, Zerg.Drone),
            Get(Zerg.SpawningPool),
            Get(14, Zerg.Drone))))),
  
    new If(
      new Employing(ZvTProxyHatchZerglings),
      new Parallel(
        new AddSupplyWhenSupplyBlocked,
        new Pump(Zerg.Zergling),
        new Trigger(
          new WeKnowWhereToProxy,
          new Pump(Zerg.Hatchery, 5, 1)))),
  
    new If(
      new Employing(ZvTProxyHatchHydras),
      new Parallel(
        new CapGasAt(50, 175, 2.0 / 12.0),
        new Build(Get(Zerg.HydraliskDen)),
        new RequireSufficientSupply,
        new If(
          new UnitsAtLeast(1, Zerg.HydraliskDen, complete = false),
          new Pump(Zerg.Hydralisk),
          new Pump(Zerg.Zergling)),
        new UpgradeContinuously(Zerg.HydraliskSpeed),
        new UpgradeContinuously(Zerg.HydraliskRange))),
  
    new If(
      new Employing(ZvTProxyHatchSunkens),
      new Parallel(
        new Pump(Zerg.SunkenColony),
        new Trigger(
          new UnitsAtLeast(2, Zerg.Hatchery, complete = true),
          initialAfter = new Trigger(
            new WeHaveEnoughSunkens,
            initialBefore = new Parallel(
              new Pump(Zerg.CreepColony, 2),
              new Pump(Zerg.Zergling)),
            initialAfter = new Parallel(
              new RequireSufficientSupply,
              new Pump(Zerg.Mutalisk),
              new Build(Get(24, Zerg.Drone)),
              new BuildGasPumps,
              new Pump(Zerg.Zergling),
              new Build(
                Get(Zerg.Lair),
                Get(Zerg.ZerglingSpeed),
                Get(Zerg.Spire)),
              new If(
                new UnitsAtMost(6, Zerg.SunkenColony),
                new Pump(Zerg.CreepColony, 1)),
              new Pump(Zerg.Hatchery, 8)
            ))))),
      
    new If(new Not(new WeKnowWhereToProxy), new ScoutAt(8, 2)),
    
    new Aggression(1.5),
    new Attack,
    new FollowBuildOrder,
  
    new If(
      new Employing(ZvTProxyHatchSunkens),
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
