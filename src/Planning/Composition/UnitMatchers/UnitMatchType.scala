package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchType(unitClass: UnitClass) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass == unitClass && unit.alive
}
