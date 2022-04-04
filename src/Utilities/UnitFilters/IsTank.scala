package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsTank extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTank
}
