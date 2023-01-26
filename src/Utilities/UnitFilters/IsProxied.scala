package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsProxied extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.proxied
}