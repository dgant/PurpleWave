package Plans.Generic.Allocation

import bwapi.UpgradeType

class LockCurrencyForUpgrade(upgradeType: UpgradeType, level:Int)
  extends LockCurrency {
    minerals = upgradeType.mineralPrice(level)
    gas = upgradeType.gasPrice(level)
}
