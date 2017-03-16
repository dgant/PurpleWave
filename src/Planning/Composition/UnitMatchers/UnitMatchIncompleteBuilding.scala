package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class UnitMatchIncompleteBuilding(unitClass:UnitClass) extends UnitMatchType(unitClass) {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitClass &&
      ! unit.complete &&
      unit.getBuildUnit.nonEmpty
  }
}
