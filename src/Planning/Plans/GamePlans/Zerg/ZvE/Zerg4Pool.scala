package Planning.Plans.GamePlans.Zerg.ZvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{ExtractorTrick, Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder, FollowBuildOrder}
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Predicates.{Never, StartPositionsAtLeast}
import Planning.Plans.Scouting.{ChillOverlords, FoundEnemyBase, Scout}
import ProxyBwapi.Races.Zerg

class Zerg4Pool extends Parallel {
  
  //class FivePool extends StartPositionsAtLeast(4)
  class FivePool extends Never
  
  children.set(Vector(
  
    new If(
      new Latch(
        new And(
          new Check(() => With.self.supplyUsed >= 18),
          new UnitsAtLeast(2, Zerg.Larva)),
        GameTime(0, 10)()),
      new Aggression(99),
      new Aggression(2.0)),
    
    new Do(() => {
      With.blackboard.gasTargetRatio = 0
      With.blackboard.gasLimitFloor = 0
      With.blackboard.gasLimitCeiling = 0
    }),
    
    new If(
      new FivePool,
      new BuildOrder(RequestAtLeast(5, Zerg.Drone))),
  
    new If(
      new And(
        new StartPositionsAtLeast(4),
        new Not(new FoundEnemyBase)),
      new Scout { scouts.get.unitMatcher.set(Zerg.Overlord) }),
    
    new BuildOrder(
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(5, Zerg.Drone)),
    new Build(RequestAtLeast(1, Zerg.Overlord)),
  
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.SpawningPool),
        new UnitsAtLeast(4, Zerg.Drone),
        new Or(
          new FivePool,
          new Latch(new MineralsAtLeast(126))),
          new Scout)),
    new ExtractorTrick,
    new TrainContinuously(Zerg.Zergling),
    
    new Attack,
    new FollowBuildOrder,
    new Gather,
    new ChillOverlords
  ))
}
