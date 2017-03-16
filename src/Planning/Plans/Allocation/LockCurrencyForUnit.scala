package Planning.Plans.Allocation

import ProxyBwapi.UnitClass.UnitClass

class LockCurrencyForUnit(unitType: UnitClass)
  extends LockCurrency {
  minerals = unitType.mineralPrice
  gas = unitType.gasPrice
  supply = unitType.supplyRequired
}
