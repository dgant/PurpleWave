package Planning.Composition.ResourceLocks

import ProxyBwapi.Upgrades.Upgrade

class LockCurrencyForUpgrade(upgradeType: Upgrade, level:Int) extends LockCurrency {
  minerals = upgradeType.mineralPrice(level)
  gas = upgradeType.gasPrice(level)
}
