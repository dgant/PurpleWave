package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchNotHoldingResources extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = ! unit.carryingResources
}
