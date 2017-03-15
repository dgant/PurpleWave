package Planning.Plans.Allocation

import bwapi.UnitType

class LockCurrencyForUnit(unitType: UnitType)
  extends LockCurrency {
  minerals = unitType.mineralPrice()
  gas = unitType.gasPrice()
  supply = unitType.supplyRequired()
}
