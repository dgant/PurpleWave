package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAnything extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = true
}
