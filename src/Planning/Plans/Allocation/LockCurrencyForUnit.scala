package Planning.Plans.Allocation

import ProxyBwapi.UnitClass.UnitClass

class LockCurrencyForUnit(unitClass: UnitClass) extends LockCurrency {
  
  description.set(unitClass.toString)
  
  minerals  = unitClass.mineralPrice
  gas       = unitClass.gasPrice
  supply    = unitClass.supplyRequired
}
