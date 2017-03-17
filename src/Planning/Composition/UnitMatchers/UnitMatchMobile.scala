package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitMatchMobile extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.unitClass.canMove
  }
}
