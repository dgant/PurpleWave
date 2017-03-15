package Planning.Plans.Allocation

import Startup.With
import Planning.Plan

class LockCurrency
  extends Plan {
  
  var isSatisfied = false
  var minerals = 0
  var gas = 0
  var supply = 0
  var isSpent = false
  
  override def isComplete:Boolean = { isSatisfied }
  
  override def onFrame() {
    With.bank.add(this)
  }
}
