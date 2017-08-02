package Planning.Plans.Terran

import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Plans.Army.{ConsiderAttacking, DefendChokes}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildRefineries, RequireMiningBases}
import ProxyBwapi.Races.Terran

class DestinyCloudFist extends Parallel {
  
  children.set(Vector(
    new RequireMiningBases(1),
    new FirstEightMinutes(
      new Build(
        RequestAtLeast(1, Terran.CommandCenter),
        RequestAtLeast(9, Terran.SCV),
        RequestAtLeast(1, Terran.SupplyDepot),
        RequestAtLeast(11, Terran.SCV),
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(13, Terran.SCV),
        RequestAtLeast(1, Terran.Refinery))),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Terran.Marine),
    new RequireMiningBases(2),
    new BuildRefineries,
    new TrainContinuously(Terran.Barracks, 2),
    new Build(RequestAtLeast(1, Terran.Factory)),
    new Build(RequestAtLeast(1, Terran.MachineShop)),
    new Build(RequestTech(Terran.SiegeMode)),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new Build(RequestAtLeast(1, Terran.Starport)),
    new TrainContinuously(Terran.Wraith),
    new Build(RequestAtLeast(1, Terran.EngineeringBay)),
    new Build(RequestAtLeast(3, Terran.MissileTurret)),
    new Build(RequestAtLeast(3, Terran.Factory)),
    new Build(RequestAtLeast(3, Terran.MachineShop)),
    new TrainContinuously(Terran.Barracks),
    new ConsiderAttacking,
    new DefendChokes,
    new FollowBuildOrder,
    new Gather
  ))
}