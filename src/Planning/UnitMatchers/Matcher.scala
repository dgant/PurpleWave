package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

trait Matcher {
  def apply(unit: UnitInfo): Boolean
}
