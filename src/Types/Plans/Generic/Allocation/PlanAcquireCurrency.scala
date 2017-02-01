package Types.Plans.Generic.Allocation

import Startup.With
import Types.Plans.Plan
import Types.Traits.CurrencyRequest

class PlanAcquireCurrency extends Plan with CurrencyRequest {
  
  override def isComplete(): Boolean = { requestFulfilled }
  
  override def execute() {
    With.bank.add(this)
  }
  
  override def abort() {
    With.bank.remove(this)
  }
}
