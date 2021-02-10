package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.MatchWorkers

class Gather extends Prioritized {

  val workerLock: LockUnits = new LockUnits
  workerLock.matcher.set(MatchWorkers)

  def update() {
    workerLock.acquire(this)
    With.gathering.gatheringPlan = this
    With.gathering.workers = workerLock.units
  }
}