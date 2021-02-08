package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchWorkers

class Gather extends Prioritized {

  val workerLock: LockUnits = new LockUnits
  workerLock.unitMatcher.set(UnitMatchWorkers)

  def update() {
    workerLock.acquire(this)
    With.gathering.gatheringPlan = this
    With.gathering.workers = workerLock.units
  }
}