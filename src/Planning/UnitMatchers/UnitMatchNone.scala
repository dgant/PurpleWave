package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchNone extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = false
}
