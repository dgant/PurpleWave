package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class UnitMatchType(unitClass:UnitClass) extends UnitMatcher {
  
  override def accept(unit: FriendlyUnitInfo): Boolean =
    unit.unitClass == unitClass && unit.aliveAndComplete
}
