package UnitMatchers

import bwapi.{Unit, UnitType}

class UnitMatchTypeIncomplete(unitType:UnitType) extends UnitMatchType(unitType) {
  override def accept(unit: Unit): Boolean = {
    super.accept(unit) && ! unit.isCompleted
  }
}
