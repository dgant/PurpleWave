package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchMobileFlying, UnitMatchWorkers}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.Zerg

class Zerg9PoolProxySunkens extends Parallel {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  private def blueprintCreepColony: Blueprint = new Blueprint(this,
    building          = Some(Zerg.CreepColony),
    requireZone       = ProxyPlanner.proxyEnemyMain,
    placement  = Some(PlacementProfiles.proxyCannon))
  
  children.set(Vector(
    new If(
      new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
      new Parallel(
        new ProposePlacement {
          override lazy val blueprints = Vector(
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony,
            blueprintCreepColony)
        })),
          
    new Build(
      RequestAtLeast(1, Zerg.Hatchery),
      RequestAtLeast(1, Zerg.Drone),
      RequestAtLeast(1, Zerg.Overlord),
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(2, Zerg.Overlord)),
  
    new If(
      new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new TrainContinuously(Zerg.Zergling),
        new TrainContinuously(Zerg.CreepColony, 2)),
      new TrainContinuously(Zerg.Zergling)),
    
    new Attack,
    new FollowBuildOrder,
    new If(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = false),
      new If(
        new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
        new Attack {
          attackers.get.unitMatcher.set(UnitMatchWorkers)
          attackers.get.unitCounter.set(UnitCountExactly(2))
        },
        new Scout(2))),
    new Attack { attackers.get.unitMatcher.set(UnitMatchMobileFlying) },
    new Gather
  ))
}
