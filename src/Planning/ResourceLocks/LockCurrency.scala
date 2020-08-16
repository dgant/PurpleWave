package Planning.ResourceLocks

import Lifecycle.With
import Planning.Plan

class LockCurrency {
  
  var framesPreordered  = 0
  var minerals          = 0
  var gas               = 0
  var supply            = 0
  var isSpent           = false
  var isSatisfied       = false
  var expectedFrames    = 0
  var owner: Plan       = _
  
  def satisfied: Boolean = isSatisfied || isSpent
  
  def acquire(plan: Plan) {
    owner = plan
    With.bank.request(this)
  }
  
  def onSchedule: Boolean = expectedFrames <= framesPreordered
}
