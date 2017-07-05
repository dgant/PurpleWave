package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAnother
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Compound.{Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import ProxyBwapi.Races.Protoss

class ProbeRush extends Parallel(
  new If(
    // Gather with the 5th Probe, not the first four.
    new Check(() => With.frame > 24 * 21),
    new Gather {
      description.set("Gather")
      workers.unitCounter.set(UnitCountOne)
    }),
  new AttackWithWorkers,
  new If(
    new Check(() => With.self.supplyUsed == With.self.supplyTotal),
    new Build(RequestUnitAnother(1, Protoss.Pylon))
  ),
  new TrainContinuously(Protoss.Probe),
  new FollowBuildOrder
)