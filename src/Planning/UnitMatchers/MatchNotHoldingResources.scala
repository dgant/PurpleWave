package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNotHoldingResources extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = ! unit.carrying
}
