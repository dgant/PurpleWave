package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsVisible extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.visible
}
