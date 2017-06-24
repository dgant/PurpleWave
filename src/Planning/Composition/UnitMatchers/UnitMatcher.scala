package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

trait UnitMatcher {
  
  def reset() {}
  def accept(unit: UnitInfo): Boolean
}
