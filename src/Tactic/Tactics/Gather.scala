package Tactic.Tactics

import Debugging.{SimpleString, ToString}
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitFilters.IsWorker

class Gather extends Tactic with SimpleString {

  val workerLock: LockUnits = new LockUnits(this)
  workerLock.matcher = IsWorker

  def launch(): Unit = {
    workerLock.acquire()
    With.gathering.setWorkers(workerLock.units)
  }
}