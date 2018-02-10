package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Matchup.EnemyIsZerg
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast, UpgradeComplete}
import ProxyBwapi.Races.{Terran, Zerg}

class NineHatchLings extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Hatchery),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(10, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(6, Zerg.Zergling))
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new Not(new EnemyIsZerg)),
    new Attack,
    new Attack { attackers.get.unitCounter.set(UnitCountOne) })
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Or(
        new EnemyUnitsAtLeast(1, Zerg.Mutalisk),
        new EnemyUnitsAtLeast(1, Terran.Vulture)),
      new AllIn
    ),
    new Build(RequestAtLeast(9, Zerg.Drone)),
    new TrainContinuously(Zerg.Zergling),
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
        new Build(
          RequestAtLeast(1, Zerg.Extractor),
          RequestUpgrade(Zerg.ZerglingSpeed)),
        new Trigger(
          new Check(() => With.self.gas >= 100),
          new Do(() => {
            With.blackboard.gasTargetRatio = 0
            With.blackboard.gasLimitFloor = 0
            With.blackboard.gasLimitCeiling = 0
          })),
        new If(
          new Check(() => With.self.minerals > 280),
          new Build(RequestAtLeast(3, Zerg.Hatchery)))))
  )
}
