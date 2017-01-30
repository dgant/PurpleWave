package Types.Requirements

import Startup.With
import Types.Contracts.{Buyer, ContractCurrency, PriorityMultiplier}

class RequireCurrency (
  buyer:Buyer,
  priorityMultiplier: PriorityMultiplier,
  val minerals:Integer,
  val gas:Integer,
  val supply:Integer)
    extends Requirement(
      buyer,
      priorityMultiplier: PriorityMultiplier) {
  
  var contract:Option[ContractCurrency] = None
  
  override def fulfill() {
    abort()
    contract = Some(With.bank.fulfill(this, priorityMultiplier))
    isFulfilled = true
  }
  
  override def abort() = {
    contract.foreach(With.bank.abort)
    contract = None
    isFulfilled = false
  }
}
