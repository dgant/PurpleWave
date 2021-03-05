package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

class MatchSpecific(units: Set[UnitInfo] = Set.empty) extends UnitMatcher {
  @inline final override def apply(unit: UnitInfo): Boolean = units.contains(unit)
}
