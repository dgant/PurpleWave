package Types.Plans.Generic.Allocation

import bwapi.UnitType

class PlanAcquireCurrencyForUnit(unitType: UnitType)
  extends PlanAcquireCurrency {
  minerals = unitType.mineralPrice()
  gas = unitType.gasPrice()
  supply = unitType.supplyRequired()
}
