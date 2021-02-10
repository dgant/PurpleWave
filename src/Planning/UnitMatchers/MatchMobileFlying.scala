package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobileFlying extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean =
    unit.canMove && unit.flying
}
