package Planning.Composition.ResourceLocks

import Startup.With
import Planning.Plan

class LockCurrency extends ResourceLock {
  
  var minerals    = 0
  var gas         = 0
  var supply      = 0
  var isSpent     = false
  var isSatisfied = false
  var owner:Plan  = null
  
  override def isComplete:Boolean = isSatisfied
  override def acquire(plan:Plan) {
    owner = plan
    With.bank.request(this)
  }
  
  override def release() = {
    With.bank.release(this)
  }
}
