package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

class UnitMatchSpecific(defaultUnits:Set[UnitInfo] = Set.empty) extends UnitMatcher {
  
  var specificUnits:Set[UnitInfo] = defaultUnits
  
  override def accept(unit: UnitInfo): Boolean = {
    specificUnits.contains(unit)
  }
}
