package Planning.ResourceLocks

import Lifecycle.With
import Macro.Allocation.Prioritized

class LockCurrency(prioritized: Prioritized) {

  var minerals            = 0
  var gas                 = 0
  var supply              = 0
  var satisfied           = false
  var expectedFrames      = 0
  var owner: Prioritized  = _
  
  def acquire(): Boolean = {
    owner = prioritized
    owner.prioritize()
    With.bank.request(this)
    satisfied
  }
}
