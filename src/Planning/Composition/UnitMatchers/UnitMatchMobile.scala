package Planning.Composition.UnitMatchers
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

class UnitMatchMobile extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype.canMove
  }
}
