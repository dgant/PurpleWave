package Tactic.Tactics

import Debugging.SimpleString
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitFilters.IsWorker

class TacticGather extends Tactic with SimpleString {

  val workerLock: LockUnits = new LockUnits(this, IsWorker)

  def launch(): Unit = {
    With.gathering.setWorkers(workerLock.acquire())
  }
}