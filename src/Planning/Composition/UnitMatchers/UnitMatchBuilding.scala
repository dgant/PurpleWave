package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchBuilding extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
