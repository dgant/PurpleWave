package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo
import bwapi.UnitType

class UnitMatchType(unitType:UnitType) extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitType && unit.complete
  }
}
