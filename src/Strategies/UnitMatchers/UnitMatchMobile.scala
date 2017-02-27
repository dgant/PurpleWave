package Strategies.UnitMatchers
import Types.UnitInfo.FriendlyUnitInfo

class UnitMatchMobile extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.unitType.canMove
  }
}
