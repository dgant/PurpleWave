package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchBuilding extends Matcher {
  @inline override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
