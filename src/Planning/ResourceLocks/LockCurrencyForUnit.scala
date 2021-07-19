package Planning.ResourceLocks

import Planning.Prioritized
import ProxyBwapi.UnitClasses.UnitClass

class LockCurrencyForUnit(prioritized: Prioritized, unitClass: UnitClass) extends LockCurrency(prioritized) {
  minerals  = unitClass.mineralPrice
  gas       = unitClass.gasPrice
  supply    = unitClass.supplyRequired
}
