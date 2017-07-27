package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobileFlying extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    unit.canMove && unit.flying
}
