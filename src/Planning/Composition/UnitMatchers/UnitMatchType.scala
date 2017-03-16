package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitMatchType(unitClass:UnitClass) extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitClass && unit.complete
  }
}
