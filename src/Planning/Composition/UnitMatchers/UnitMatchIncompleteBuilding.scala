package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitMatchIncompleteBuilding(unitType:UnitClass) extends UnitMatchType(unitType) {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitType &&
      ! unit.complete &&
      unit.getBuildUnit.nonEmpty
  }
}
