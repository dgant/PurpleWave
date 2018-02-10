package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import ProxyBwapi.Races.Zerg

class TwoHatchMuta extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(12, Zerg.Drone))
  
  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(1, Zerg.Extractor)),
    new If(
      new Check(() => With.self.gas >  80 || With.self.minerals < 200),
      new TrainContinuously(Zerg.Mutalisk)),
    new If(
      new Check(() => With.geography.ourBases.exists(_.units.exists(u =>
        u.isEnemy
        && u.unitClass.attacksGround
        && ! u.unitClass.isWorker))),
      new TrainContinuously(Zerg.Zergling)),
    new TrainContinuously(Zerg.Drone, 16),
    new Build(
      RequestAtLeast(1, Zerg.Lair),
      RequestUpgrade(Zerg.ZerglingSpeed),
      RequestAtLeast(2, Zerg.Extractor),
      RequestAtLeast(1, Zerg.Spire)),
    new TrainContinuously(Zerg.Zergling),
    new TrainContinuously(Zerg.Hatchery, 5, 1)
  )
}
