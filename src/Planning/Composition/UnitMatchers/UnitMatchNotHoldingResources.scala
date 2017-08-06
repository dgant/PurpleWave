package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchNotHoldingResources extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = ! unit.carryingResources
}
