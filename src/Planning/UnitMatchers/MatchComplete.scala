package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchComplete extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.complete
}
