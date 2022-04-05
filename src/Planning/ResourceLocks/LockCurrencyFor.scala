package Planning.ResourceLocks

import Planning.Prioritized
import ProxyBwapi.Buildable

class LockCurrencyFor(prioritized: Prioritized, buildableType: Buildable, upgradeLevel: Int) extends LockCurrency(prioritized) {
  minerals  = buildableType.mineralCost(upgradeLevel)
  gas       = buildableType.gasCost(upgradeLevel)
  supply    = buildableType.supplyRequired
}
