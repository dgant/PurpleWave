package Planning.ResourceLocks

import Planning.Prioritized
import ProxyBwapi.Upgrades.Upgrade

class LockCurrencyForUpgrade(prioritized: Prioritized, upgradeType: Upgrade, level:Int) extends LockCurrency(prioritized) {
  minerals = upgradeType.mineralPrice(level)
  gas = upgradeType.gasPrice(level)
}
