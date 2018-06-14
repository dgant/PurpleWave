package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchHasNuke extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.hasNuke
  
}
