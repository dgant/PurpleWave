package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsHatchlike extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isHatchlike
}
