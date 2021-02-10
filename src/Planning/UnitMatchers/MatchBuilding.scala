package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchBuilding extends UnitMatcher {
  @inline override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
