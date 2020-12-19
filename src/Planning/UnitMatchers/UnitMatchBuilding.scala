package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchBuilding extends UnitMatcher {
  @inline override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
