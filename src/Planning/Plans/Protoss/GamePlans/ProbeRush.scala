package Planning.Plans.Protoss.GamePlans

import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.FollowBuildOrder
import ProxyBwapi.Races.Protoss

class ProbeRush extends Parallel(
  new Gather {
    description.set("Gather")
    workers.unitCounter.set(UnitCountOne)
  },
  new AttackWithWorkers,
  new TrainContinuously(Protoss.Probe),
  new FollowBuildOrder
)
