package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNone extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = false
}
