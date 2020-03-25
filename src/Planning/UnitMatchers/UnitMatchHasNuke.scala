package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchHasNuke extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.hasNuke
  
}
