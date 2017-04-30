package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitMatchMobileFlying extends UnitMatcher {
  
  override def accept(unit: FriendlyUnitInfo): Boolean =
    unit.canMoveThisFrame && unit.flying
}
