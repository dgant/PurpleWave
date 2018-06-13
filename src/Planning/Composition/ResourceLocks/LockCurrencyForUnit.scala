package Planning.Composition.ResourceLocks

import ProxyBwapi.UnitClasses.UnitClass

class LockCurrencyForUnit(unitClass: UnitClass) extends LockCurrency {
  minerals  = unitClass.mineralPrice
  gas       = unitClass.gasPrice
  supply    = unitClass.supplyRequired
}
