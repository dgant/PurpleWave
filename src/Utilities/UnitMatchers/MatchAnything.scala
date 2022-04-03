package Utilities.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object MatchAnything extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean = true
}
