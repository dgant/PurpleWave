package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchType(unitClass: UnitClass) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass == unitClass && unit.alive
}
