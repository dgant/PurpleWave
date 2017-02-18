package Plans.Allocation

import bwapi.{TechType}

class LockCurrencyForTech(techType: TechType)
  extends LockCurrency {
    minerals = techType.mineralPrice()
    gas = techType.gasPrice()
}
