package Types.Requirements

import Startup.With

class RequireCurrency (
  val minerals:Integer,
  val gas:Integer,
  val supply:Integer)
    extends Requirement {
  
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
