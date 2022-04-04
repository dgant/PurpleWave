package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsAntiAir extends UnitFilter {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitAir > 0
}
