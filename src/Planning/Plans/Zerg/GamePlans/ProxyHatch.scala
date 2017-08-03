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
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.ScoutAt
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.Global.{ProxyHatchHydras, ProxyHatchSunkens, ProxyHatchZerglings}

class ProxyHatch extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  val buildUpToHatchery = Vector(
    RequestAtLeast(8,   Zerg.Drone),
    RequestAtLeast(2,   Zerg.Overlord),
    RequestAtLeast(13,  Zerg.Drone))
  
  val buildUpToHydras = Vector(
    RequestAtLeast(1,   Zerg.Extractor),
    RequestAtLeast(3,   Zerg.Overlord),
    RequestAtLeast(1,   Zerg.HydraliskDen),
    RequestAtLeast(15,  Zerg.Drone),
    RequestAtLeast(4,   Zerg.Overlord))
  
  private def blueprintCreepColonyNatural: Blueprint = new Blueprint(this,
    building          = Some(Zerg.CreepColony),
    requireZone       = ProxyPlanner.proxyEnemyNatural,
    placementProfile  = Some(PlacementProfiles.proxyCannon))
  children.set(Vector(
    new Plan { override def onUpdate(): Unit = {
      With.blackboard.gasBankSoftLimit = if (With.units.ours.exists(_.is(Zerg.Lair))) 400 else 75
      With.blackboard.gasBankHardLimit = With.blackboard.gasBankSoftLimit
    }},
    new ProposePlacement { override lazy val blueprints = Vector(new Blueprint(this,
      building = Some(Zerg.Extractor),
      requireZone = Some(With.geography.ourMain.zone))) },
    new Build(
      RequestAtLeast(8,   Zerg.Drone),
      RequestAtLeast(2,   Zerg.Overlord),
      RequestAtLeast(9,   Zerg.Drone)),
  
    new If(
      new Check(() => ProxyPlanner.proxyEnemyNatural.isDefined),
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
          blueprintCreepColonyNatural)},
        new Employ(ProxyHatchHydras,
          new Build(
            RequestAtLeast(2, Zerg.Hatchery),
            RequestAtLeast(1, Zerg.SpawningPool),
            RequestAtLeast(11, Zerg.Drone),
            RequestAtLeast(1, Zerg.Extractor), //We get it a bit earlier than required to overcome a bug in which we refuse to gather gas after the second hatch has spawned
            RequestAtLeast(12, Zerg.Drone))),
        new Employ(ProxyHatchZerglings,
          new Build(
            RequestAtLeast(2, Zerg.Hatchery),
            RequestAtLeast(1, Zerg.SpawningPool))),
        new Employ(ProxyHatchSunkens,
          new Build(
            RequestAtLeast(2, Zerg.Hatchery),
            RequestAtLeast(1, Zerg.SpawningPool),
            RequestAtLeast(12, Zerg.Drone))))),
  
    new RequireSufficientSupply,
    new If(
      new UnitsAtLeast(1, UnitMatchType(Zerg.HydraliskDen), complete = false),
      new TrainContinuously(Zerg.Hydralisk),
      new TrainContinuously(Zerg.Zergling)),
  
    new Employ(ProxyHatchZerglings, new TrainContinuously(Zerg.Hatchery)),
    new Employ(ProxyHatchHydras, new Build(RequestAtLeast(1, Zerg.HydraliskDen))),
  
    new Employ(ProxyHatchSunkens, new If(
      new And(
        new UnitsAtLeast(2, UnitMatchType(Zerg.Hatchery),     complete = true),
        new UnitsAtMost(6, UnitMatchType(Zerg.SunkenColony),  complete = false)),
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new TrainContinuously(Zerg.CreepColony, 2)),
      new Parallel(
        new TrainWorkersContinuously,
        new Build(RequestAtLeast(1, Zerg.Lair)),
        new RequireMiningBases(3),
        new BuildGasPumps,
        new Build(RequestAtLeast(1, Zerg.Spire)),
        new TrainContinuously(Zerg.Mutalisk)
      ))),
      
    new If(
      new Check(() => ProxyPlanner.proxyEnemyNatural.isEmpty),
      new ScoutAt(8, 2)),
    
    new Aggression(2.0),
    new Attack,
    new FollowBuildOrder,
  
    new Employ(ProxyHatchSunkens,
      new If(
        new And(
          new UnitsAtLeast(2, UnitMatchType(Zerg.Hatchery),     complete = false),
          new UnitsAtLeast(1, UnitMatchType(Zerg.SpawningPool), complete = false)),
        new Attack {
          attackers.get.unitMatcher.set(UnitMatchWorkers)
          attackers.get.unitCounter.set(UnitCountExactly(2))
        },
        new Attack { attackers.get.unitMatcher.set(UnitMatchMobileFlying) })),
    
    new Gather
  ))
}
