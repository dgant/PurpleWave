package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestAnother
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
    new Build(RequestAnother(1, Protoss.Pylon))
  ),
  new TrainContinuously(Protoss.Probe),
  new FollowBuildOrder,
  new Trigger(
    new UnitsAtLeast(5, UnitMatchWorkers, complete = true),
    new Gather {
      workers.unitCounter.set(UnitCountExactly(1))
      workers.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
      workers.interruptable.set(false) // Note that this means we can't build more workers until another Probe spawns. But that also means Probes aren't dying.
    }),
  new AttackWithWorkers
)