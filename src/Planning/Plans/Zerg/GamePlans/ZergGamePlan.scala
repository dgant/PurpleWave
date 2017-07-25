package Planning.Plans.Zerg.GamePlans

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.{Attack, DefendChokes}
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RemoveMineralBlockAt
import ProxyBwapi.Races.Zerg

class ZergGamePlan extends Parallel {
 
  children.set(Vector(
    new Build(
      RequestAtLeast(3,   Zerg.Drone),
      RequestAtLeast(1,   Zerg.SpawningPool),
      RequestAtLeast(4,   Zerg.Drone),
      RequestAtLeast(10,  Zerg.Zergling)),
    new Attack,
    new FollowBuildOrder,
    new RemoveMineralBlockAt(30),
    new Gather,
    new DefendChokes
  ))
}
