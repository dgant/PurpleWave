package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchEnemy extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = unit.isEnemy
}
