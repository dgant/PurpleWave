package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchAlive extends Matcher {
  override def apply(unit: UnitInfo): Boolean = unit.alive
}
