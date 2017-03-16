package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitMatchType(unitType:UnitClass) extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitType && unit.complete
  }
}
