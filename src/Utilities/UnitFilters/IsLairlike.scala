package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsLairlike extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isLairlike
}
