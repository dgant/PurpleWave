package Tactics

import Debugging.ToString
import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.MatchWorkers

class Gather extends Prioritized {

  val workerLock: LockUnits = new LockUnits(this)
  workerLock.matcher = MatchWorkers

  def update() {
    workerLock.acquire(this)
    With.gathering.gatheringPlan = this
    With.gathering.workers = workerLock.units
  }

  override val toString = ToString(this)
}