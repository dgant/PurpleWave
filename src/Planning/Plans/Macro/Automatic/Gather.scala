package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchWorkers

class Gather extends Plan {

  val workerLock: LockUnits = new LockUnits
  workerLock.unitMatcher.set(UnitMatchWorkers)

  override def onUpdate() {
    workerLock.acquire(this)
    With.gathering.gatheringPlan = this
    With.gathering.workers = workerLock.units
  }
}