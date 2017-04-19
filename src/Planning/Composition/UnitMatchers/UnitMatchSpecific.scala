package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class UnitMatchSpecific(defaultUnits:Set[UnitInfo] = Set.empty) extends UnitMatcher {
  
  var specificUnits:Set[UnitInfo] = defaultUnits
  
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    specificUnits.contains(unit)
  }
}
