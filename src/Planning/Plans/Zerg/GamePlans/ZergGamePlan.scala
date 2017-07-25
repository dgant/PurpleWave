package Planning.Plans.Zerg.GamePlans

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWorkers}
import Planning.Plans.Army.{Attack, DefendChokes}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RemoveMineralBlockAt
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.FindEnemyBase
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
      new UnitsAtLeast(1, UnitMatchType(Zerg.SpawningPool), complete = true),
      new Attack {
        attackers.get.unitCounter.set(UnitCountOne)
        attackers.get.unitMatcher.set(UnitMatchWorkers)
      }),
    new Attack,
    new FollowBuildOrder,
    new RemoveMineralBlockAt(30),
    new Gather,
    new DefendChokes
  ))
}
