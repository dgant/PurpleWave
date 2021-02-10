package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchComplete extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.complete
}
