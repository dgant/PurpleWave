package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

class MatchSpecific(defaultUnits: Set[UnitInfo] = Set.empty) extends UnitMatcher {
  
  var specificUnits: Set[UnitInfo] = defaultUnits
  
  override def apply(unit: UnitInfo): Boolean = {
    specificUnits.contains(unit)
  }
}
