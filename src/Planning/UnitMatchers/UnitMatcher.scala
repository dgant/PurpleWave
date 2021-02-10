package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

trait UnitMatcher {
  def apply(unit: UnitInfo): Boolean
}
