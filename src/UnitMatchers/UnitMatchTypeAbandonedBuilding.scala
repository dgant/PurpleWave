package UnitMatchers

import bwapi.{Unit, UnitType}

class UnitMatchTypeAbandonedBuilding(unitType:UnitType) extends UnitMatchType(unitType) {
  override def accept(unit: Unit): Boolean = {
    unit.getType == unitType &&
      ! unit.isCompleted &&
      unit.getBuildUnit == null
  }
}
