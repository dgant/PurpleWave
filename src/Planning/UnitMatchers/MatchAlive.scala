package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAlive extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.alive
}
