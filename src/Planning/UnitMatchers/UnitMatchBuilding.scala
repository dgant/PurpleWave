package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchBuilding extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
