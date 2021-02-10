package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchHasNuke extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.hasNuke
  
}
