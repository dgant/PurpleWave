package Planning.ResourceLocks

import Lifecycle.With
import Planning.Prioritized

class LockCurrency(prioritized: Prioritized) {
  
  var framesPreordered    = 0
  var minerals            = 0
  var gas                 = 0
  var supply              = 0
  var isSpent             = false
  var isSatisfied         = false
  var expectedFrames      = 0
  var owner: Prioritized  = _
  
  def satisfied: Boolean = isSatisfied || isSpent
  
  def acquire() {
    owner = prioritized
    owner.prioritize()
    With.bank.request(this)
  }
  
  def onSchedule: Boolean = expectedFrames <= framesPreordered
}
