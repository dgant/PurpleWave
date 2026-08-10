package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Planning.ResourceLocks.LockCurrency

class ProduceMicroSlush(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val currencyLock = new LockCurrency(this)

  override def isComplete: Boolean = false
  override def hasSpent: Boolean = false

  override def onUpdate(): Unit = {
    currencyLock.minerals = request.mineralCost
    currencyLock.acquire()
    With.microSlush.allocate(currencyLock.mineralsGranted)
  }
}
