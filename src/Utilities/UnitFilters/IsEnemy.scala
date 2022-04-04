package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsEnemy extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.isEnemy
}
