package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobileFlying extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean =
    unit.canMove && unit.flying
}
