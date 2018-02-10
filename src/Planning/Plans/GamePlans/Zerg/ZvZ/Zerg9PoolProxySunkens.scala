package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Zerg

class Zerg9PoolProxySunkens extends GameplanModeTemplate {
  
  override def onUpdate() {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  private def blueprintCreepColony: Blueprint = new Blueprint(this,
    building    = Some(Zerg.CreepColony),
    requireZone = ProxyPlanner.proxyEnemyMain,
    placement   = Some(PlacementProfiles.proxyCannon))
  
  override def defaultPlacementPlan: Plan =
    new If(
      new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
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
      })
  
  override val buildOrder = Vector(
    RequestAtLeast(1, Zerg.Hatchery),
    RequestAtLeast(1, Zerg.Drone),
    RequestAtLeast(1, Zerg.Overlord),
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(10, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(11, Zerg.Drone),
    RequestAtLeast(6, Zerg.Zergling))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new TrainContinuously(Zerg.Zergling),
        new TrainContinuously(Zerg.CreepColony, 2),
        new If(
          new UnitsAtLeast(1, Zerg.SpawningPool, complete = false),
          new Attack {
            attackers.get.unitMatcher.set(UnitMatchWorkers)
            attackers.get.unitCounter.set(UnitCountExactly(3))
         })),
      new Parallel(
        new TrainContinuously(Zerg.Zergling),
        new Scout(3))))
}
