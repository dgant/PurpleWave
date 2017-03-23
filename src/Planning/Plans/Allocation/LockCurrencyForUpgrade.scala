package Planning.Plans.Allocation

import ProxyBwapi.Upgrades.Upgrade

class LockCurrencyForUpgrade(upgradeType: Upgrade, level:Int) extends LockCurrency {
  description.set(upgradeType.toString + (if(level > 1) " 1" else ""))
    
  minerals = upgradeType.mineralPrice(level)
  gas = upgradeType.gasPrice(level)
}
