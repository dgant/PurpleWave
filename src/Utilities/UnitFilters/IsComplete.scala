package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsComplete extends UnitFilter {
  
  override def apply(unit: UnitInfo): Boolean = unit.complete
}
