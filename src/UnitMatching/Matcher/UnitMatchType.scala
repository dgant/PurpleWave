package UnitMatching.Matcher

import bwapi.{Unit, UnitType}

class UnitMatchType(unitType:UnitType) extends UnitMatch {
  override def accept(unit: Unit): Boolean = {
    unit.getType == unitType
  }
}
