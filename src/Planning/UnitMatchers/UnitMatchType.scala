package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchType(unitClass: UnitClass) extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass == unitClass && unit.alive
}
