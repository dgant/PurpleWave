package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsAntiGround extends UnitFilter {
  
  override def apply(unit: UnitInfo): Boolean = unit.damageOnHitGround > 0
}
