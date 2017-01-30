package Types.Requirements

import Startup.With

class RequireCurrency (
  buyer:Buyer,
  priorityMultiplier: PriorityMultiplier,
  val minerals:Integer,
  val gas:Integer,
  val supply:Integer)
    extends Requirement(
      buyer,
      priorityMultiplier: PriorityMultiplier) {
  
  var isAvailableNow:Boolean = false
  
  override def fulfill() {
    With.bank.fulfill(this)
    isFulfilled = true
  }
  
  override def abort() = {
    With.bank.abort(this)
    isFulfilled = false
    isAvailableNow = false
  }
}
