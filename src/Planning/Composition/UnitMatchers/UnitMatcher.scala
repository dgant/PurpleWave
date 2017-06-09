package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait UnitMatcher {
  
  def accept(unit:FriendlyUnitInfo): Boolean
}
