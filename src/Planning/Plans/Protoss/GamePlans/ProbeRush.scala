package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAnother
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Compound.{Check, If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss

class ProbeRush extends Parallel(
  new If(
    new Check(() => With.self.supplyUsed == With.self.supplyTotal),
    new Build(RequestUnitAnother(1, Protoss.Pylon))
  ),
  new TrainContinuously(Protoss.Probe),
  new FollowBuildOrder,
  new Trigger(
    new UnitsAtLeast(5, UnitMatchWorkers, complete = true),
    new Gather {
      workers.unitCounter.set(UnitCountExactly(1))
      workers.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
    }),
  new AttackWithWorkers
)