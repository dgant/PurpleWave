package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Zerg

class Zerg2HatchMuta extends Parallel {
  
  children.set(Vector(
    new Build(
      RequestAtLeast(8,   Zerg.Drone),
      RequestAtLeast(2,   Zerg.Overlord),
      RequestAtLeast(12,  Zerg.Drone),
      RequestAtLeast(2,   Zerg.Hatchery),
      RequestAtLeast(14,  Zerg.Drone),
      RequestAtLeast(1,   Zerg.SpawningPool),
      RequestAtLeast(2,   Zerg.Extractor),
      RequestAtLeast(6,   Zerg.Zergling),
      RequestAtLeast(1,   Zerg.Lair),
      RequestAtLeast(21,  Zerg.Drone),
      RequestUpgrade(Zerg.ZerglingSpeed),
      RequestAtLeast(1,   Zerg.Spire)),
    new RequireSufficientSupply,
    new If(
      new Check(() => With.self.gas >= 100),
      new TrainContinuously(Zerg.Mutalisk)),
    new Trigger(
      new UnitsAtLeast(1, UnitMatchType(Zerg.Mutalisk)),
      initialAfter = new Parallel(
        new BuildGasPumps,
        new TrainWorkersContinuously,
        new TrainContinuously(Zerg.Zergling),
        new TrainContinuously(Zerg.Hatchery))),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
