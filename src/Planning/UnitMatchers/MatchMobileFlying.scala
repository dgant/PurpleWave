package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchMobileFlying extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean =
    unit.canMove && unit.flying
}
