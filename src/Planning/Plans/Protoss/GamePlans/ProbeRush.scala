package Planning.Plans.Protoss.GamePlans

import Lifecycle.With
import Macro.BuildRequests.RequestUnitAnother
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Compound.{Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import ProxyBwapi.Races.Protoss

class ProbeRush extends Parallel(
  new If(
    new Check(() => With.self.supplyUsed == With.self.supplyTotal),
    new Build(RequestUnitAnother(1, Protoss.Pylon))
  ),
  new TrainContinuously(Protoss.Probe),
  new FollowBuildOrder,
  new ProbeRushGather,
  new AttackWithWorkers
)

class ProbeRushGather extends Gather {
  
  var haveWeBuildFifthProbe = false
  
  override def onUpdate() {
    if (With.units.ours.count(unit => unit.aliveAndComplete && unit.unitClass.isWorker) >= 5) {
      haveWeBuildFifthProbe = true
    }
    
    workers.unitCounter.set(new UnitCountExactly(if (haveWeBuildFifthProbe) 1 else 0))
    workers.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
    
    super.onUpdate()
  }
}