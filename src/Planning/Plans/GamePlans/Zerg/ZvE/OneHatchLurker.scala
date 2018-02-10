package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound.{Do, If, NoPlan}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtMost
import ProxyBwapi.Races.Zerg

class OneHatchLurker extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(10, Zerg.Drone),
    RequestAtLeast(1, Zerg.Extractor),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(11, Zerg.Drone),
    RequestAtLeast(6, Zerg.Zergling))
  
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtMost(0, Zerg.Lair),
      new Do(() => {
        With.blackboard.gasTargetRatio = 3.0 / 9.0
        With.blackboard.gasLimitFloor = 100
        With.blackboard.gasLimitCeiling = 150
      }),
      new Do(() => {
        With.blackboard.gasTargetRatio = 3.0 / 9.0
        With.blackboard.gasLimitFloor = 175
        With.blackboard.gasLimitCeiling = 250
      })),
    new Build(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(11, Zerg.Drone),
      RequestAtLeast(1, Zerg.HydraliskDen)),
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen),
      new TrainContinuously(Zerg.Zergling)),
    new Build(RequestTech(Zerg.LurkerMorph)),
    new TrainContinuously(Zerg.Lurker),
    new TrainContinuously(Zerg.Hydralisk, 4, 2),
    new RequireMiningBases(2),
    new Build(RequestUpgrade(Zerg.ZerglingSpeed)),
    new TrainContinuously(Zerg.Zergling),
    new RequireMiningBases(3)
  )
}
