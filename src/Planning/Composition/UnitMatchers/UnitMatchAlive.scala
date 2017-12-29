package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAlive extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = unit.likelyStillAlive
}
