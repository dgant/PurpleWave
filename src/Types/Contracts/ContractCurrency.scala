package Types.Contracts

import Types.Requirements.RequireCurrency

class ContractCurrency(
  val requirements: RequireCurrency,
  buyer: Buyer,
  priority: PriorityMultiplier)
    extends Contract(buyer, priority) {
}
