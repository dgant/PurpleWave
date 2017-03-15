package Planning.Composition.UnitMatchers
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object UnitMatchAnything extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = true
}
