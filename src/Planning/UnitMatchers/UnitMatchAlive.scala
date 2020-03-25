package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAlive extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.likelyStillAlive
}
