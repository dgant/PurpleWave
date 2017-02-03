package Strategies.UnitMatchers
import bwapi.Unit

class UnitMatchMobile extends UnitMatcher {
  override def accept(unit: Unit): Boolean = {
    unit.canMove
  }
}
