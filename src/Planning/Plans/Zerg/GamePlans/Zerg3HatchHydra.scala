package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import ProxyBwapi.Races.Zerg

class Zerg3HatchHydra extends Parallel {
  
  children.set(Vector(
    new Build(
      RequestAtLeast(8,   Zerg.Drone),
      RequestAtLeast(2,   Zerg.Overlord),
      RequestAtLeast(12,  Zerg.Drone),
      RequestAtLeast(2,   Zerg.Hatchery),
      RequestAtLeast(16,  Zerg.Drone),
      RequestAtLeast(3,   Zerg.Hatchery),
      RequestAtLeast(1,   Zerg.SpawningPool),
      RequestAtLeast(1,   Zerg.Extractor),
      RequestAtLeast(23,  Zerg.Drone),
      RequestAtLeast(2,   Zerg.Extractor),
      RequestAtLeast(1,   Zerg.HydraliskDen),
      RequestUpgrade(Zerg.HydraliskSpeed),
      RequestUpgrade(Zerg.HydraliskRange)),
    new RequireSufficientSupply,
    new If(
      new Check(() => With.self.gas >= 25),
      new TrainContinuously(Zerg.Hydralisk)),
    new TrainWorkersContinuously,
    new TrainContinuously(Zerg.Hatchery),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
