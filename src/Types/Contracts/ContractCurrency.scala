package Types.Contracts

import Types.Requirements.RequireCurrency

class ContractCurrency(val requirement: RequireCurrency) {
  var availableRightNow:Boolean = false
}
