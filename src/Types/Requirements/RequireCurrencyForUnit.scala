package Types.Requirements

import bwapi.UnitType

class RequireCurrencyForUnit(unitType: UnitType)
  extends RequireCurrency(
    minerals = unitType.mineralPrice(),
    gas = unitType.gasPrice(),
    supply = unitType.supplyRequired()) {
  
}
