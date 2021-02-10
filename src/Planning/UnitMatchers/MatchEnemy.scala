package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchEnemy extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = unit.isEnemy
}
