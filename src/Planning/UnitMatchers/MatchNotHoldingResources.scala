package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNotHoldingResources extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = ! unit.carrying
}
