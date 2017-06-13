package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchMobile extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    unit.canMoveThisFrame
}
