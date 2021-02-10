package Planning.UnitMatchers
import ProxyBwapi.UnitInfo.UnitInfo

object MatchAnything extends Matcher {
  override def apply(unit: UnitInfo): Boolean = true
}
