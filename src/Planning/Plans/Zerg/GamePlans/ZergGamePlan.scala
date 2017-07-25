package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Attack, DefendChokes}
import Planning.Plans.Compound._
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RemoveMineralBlockAt
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.{FindEnemyBase, RequireEnemyBase}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Options.Zerg.Global.{PvZ4PoolAllIn, PvZ9Hatch9PoolAllIn}

class ZergGamePlan extends Parallel {
  
  class ImplementPvZ4PoolAllIn extends Employ(PvZ4PoolAllIn, new Build(
    RequestAtLeast(3,   Zerg.Drone),
    RequestAtLeast(1,   Zerg.SpawningPool),
    RequestAtLeast(4,   Zerg.Drone),
    RequestAtLeast(10,  Zerg.Zergling)))
  
  class ImplementPvZ9Hatch9PoolAllIn extends Employ(PvZ9Hatch9PoolAllIn, new Build(
    RequestAtLeast(9,   Zerg.Drone),
    RequestAtLeast(2,   Zerg.Hatchery),
    RequestAtLeast(1,   Zerg.SpawningPool),
    RequestAtLeast(2,   Zerg.Overlord)))
  
  class ImplementPvZ9Hatch9PoolAllIn extends Employ(PvZ9Hatch9PoolAllIn, new Build(
    RequestAtLeast(9,   Zerg.Drone),
    RequestAtLeast(2,   Zerg.Hatchery),
    RequestAtLeast(1,   Zerg.SpawningPool),
    RequestAtLeast(2,   Zerg.Overlord)))
 
  children.set(Vector(
    new ImplementPvZ4PoolAllIn,
    new ImplementPvZ9Hatch9PoolAllIn,
    new TrainContinuously(Zerg.Zergling),
    new Trigger(
      new UnitsAtLeast(1, UnitMatchType(Zerg.Zergling)),
      initialAfter = new TrainContinuously(Zerg.Hatchery)
    ),
    
    new FindEnemyBase { scouts.get.unitMatcher.set(UnitMatchType(Zerg.Overlord)) },
    new If(
      new And(
        new Check(() => With.geography.startLocations.size > 2),
        new UnitsAtLeast(1, UnitMatchType(Zerg.SpawningPool), complete = true)),
      new RequireEnemyBase),
    new Attack,
    new FollowBuildOrder,
    new RemoveMineralBlockAt(30),
    new Gather,
    new DefendChokes
  ))
}
