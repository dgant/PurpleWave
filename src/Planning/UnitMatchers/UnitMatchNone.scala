package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchNone extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = false
}
