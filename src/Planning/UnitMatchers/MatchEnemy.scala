package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchEnemy extends Matcher {
  override def apply(unit: UnitInfo): Boolean = unit.isEnemy
}
