package Tactic.Tactics

import Debugging.ToString
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitMatchers.MatchWorker

class Gather extends Tactic {

  val workerLock: LockUnits = new LockUnits(this)
  workerLock.matcher = MatchWorker

  def launch() {
    workerLock.acquire()
    With.gathering.setWorkers(workerLock.units)
  }

  override val toString = ToString(this)
}