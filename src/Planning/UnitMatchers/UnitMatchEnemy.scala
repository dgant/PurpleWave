package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchEnemy extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.isEnemy
}
