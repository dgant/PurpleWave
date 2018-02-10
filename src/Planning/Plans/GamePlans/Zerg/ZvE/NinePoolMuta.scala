package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Matchup.EnemyIsZerg
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Zerg

class NinePoolMuta extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(10, Zerg.Drone),
    RequestAtLeast(1, Zerg.Extractor),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(11, Zerg.Drone),
    RequestAtLeast(6, Zerg.Zergling))
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def defaultAttackPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(1, Zerg.Mutalisk),
      new Not(new EnemyIsZerg)),
    new Attack)
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(
      RequestAtLeast(8, Zerg.Drone),
      RequestAtLeast(1, Zerg.Lair)),
    new If(
      new UnitsAtMost(0, Zerg.Lair),
      new Do(() => {
        With.blackboard.gasTargetRatio = 3.0 / 9.0
        With.blackboard.gasLimitFloor = 100
        With.blackboard.gasLimitCeiling = 100
      }),
      new If(
        new UnitsAtMost(0, Zerg.Spire),
        new Do(() => {
          With.blackboard.gasTargetRatio = 2.0 / 9.0
          With.blackboard.gasLimitFloor = 200
          With.blackboard.gasLimitCeiling = 200
        }),
        new Do(() => {
          With.blackboard.gasTargetRatio = (if (With.self.gas > With.self.minerals) 1.0 else 3.0) / 9.0
          With.blackboard.gasLimitFloor = 100
          With.blackboard.gasLimitCeiling = 300
        }))),
    new If(
      new UnitsAtMost(0, Zerg.Spire),
      new TrainContinuously(Zerg.Zergling)),
    new Build(RequestAtLeast(1, Zerg.Spire)),
    new TrainContinuously(Zerg.Mutalisk),
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.Spire),
        new UnitsAtMost(1, Zerg.SunkenColony),
        new EnemyIsZerg),
      new Parallel(
        new TrainContinuously(Zerg.SunkenColony),
        new Build(RequestAtLeast(1, Zerg.CreepColony)))),
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.Spire, complete = true),
        new Or(
          new UnitsAtMost(0, Zerg.Larva),
          new Check(() => With.self.minerals >= 300))),
      new RequireMiningBases(2)),
    new If(
      new And(
        new Check(() => With.self.minerals >= 150),
        new Check(() => With.self.gas < 100),
        new UnitsAtLeast(2, Zerg.Larva)),
      new TrainContinuously(Zerg.Zergling))
  )
}
