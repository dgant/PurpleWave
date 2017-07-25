package Planning.Plans.Zerg.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{Attack, DefendChokes}
import Planning.Plans.Compound.{And, Check, If, Parallel}
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RemoveMineralBlockAt
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.{FindEnemyBase, RequireEnemyBase}
import ProxyBwapi.Races.Zerg

class ZergGamePlan extends Parallel {
 
  children.set(Vector(
    new Build(
      RequestAtLeast(3,   Zerg.Drone),
      RequestAtLeast(1,   Zerg.SpawningPool),
      RequestAtLeast(4,   Zerg.Drone),
      RequestAtLeast(10,  Zerg.Zergling)),
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
