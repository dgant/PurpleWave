package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNone extends Matcher {
  override def apply(unit: UnitInfo): Boolean = false
}
