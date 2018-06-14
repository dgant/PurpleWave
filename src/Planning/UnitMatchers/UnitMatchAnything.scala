package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAnything extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean = true
}
