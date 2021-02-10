package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchHasNuke extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.hasNuke
  
}
