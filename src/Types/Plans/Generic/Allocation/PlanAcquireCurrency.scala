package Types.Plans.Generic.Allocation

import Startup.With
import Types.Plans.Plan

class PlanAcquireCurrency extends Plan {
  var minerals = 0
  var gas = 0
  var supply = 0
  var requestFulfilled = false
  
  override def isComplete(): Boolean = { requestFulfilled }
  
  override def execute() {
    With.bank.add(this)
  }
  
  override def abort() {
    With.bank.remove(this)
  }
}
