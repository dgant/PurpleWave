package Planning.ResourceLocks

import Planning.Prioritized
import ProxyBwapi.BuildableType

class LockCurrencyFor(prioritized: Prioritized, buildableType: BuildableType, upgradeLevel: Int) extends LockCurrency(prioritized) {
  minerals  = buildableType.mineralCost(upgradeLevel)
  gas       = buildableType.gasCost(upgradeLevel)
  supply    = buildableType.supplyRequired
}
