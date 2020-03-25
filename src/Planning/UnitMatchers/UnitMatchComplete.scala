package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchComplete extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.complete
}
