package Tactics

import Debugging.ToString
import Lifecycle.With
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.MatchWorker

class Gather extends Prioritized {

  val workerLock: LockUnits = new LockUnits(this)
  workerLock.matcher = MatchWorker

  def update() {
    workerLock.acquire(this)
    With.gathering.setWorkers(workerLock.units)
  }

  override val toString = ToString(this)
}