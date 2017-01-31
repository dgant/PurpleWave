package UnitMatching.Matcher

import bwapi.{Unit, UnitType}

class UnitMatchType(unitType:UnitType) extends UnitMatcher {
  override def accept(unit: Unit): Boolean = {
    unit.getType == unitType
  }
}
