package Planning.Plans.Terran

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import ProxyBwapi.Races.Terran

class MassMarine extends Parallel {
  
  children.set(Vector(
    new RequireMiningBases(1),
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(8, Terran.SCV),
        RequestAtLeast(2, Terran.Barracks),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(1, Terran.Marine))),
    new RequireSufficientSupply,
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.SCV),
    new TrainContinuously(Terran.Barracks),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}