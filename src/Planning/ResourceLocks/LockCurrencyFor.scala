package Planning.ResourceLocks

import Macro.Allocation.Prioritized
import ProxyBwapi.Buildable

class LockCurrencyFor(prioritized: Prioritized, buildableType: Buildable, upgradeLevel: Int = 1) extends LockCurrency(prioritized) {
  minerals  = buildableType.mineralCost(upgradeLevel)
  gas       = buildableType.gasCost(upgradeLevel)
  supply    = buildableType.supplyRequired
}
