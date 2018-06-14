package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchComplete extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.complete
}
