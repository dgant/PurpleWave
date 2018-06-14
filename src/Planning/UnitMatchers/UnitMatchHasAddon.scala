package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchHasAddon(unitClass: UnitClass) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.addon.exists(_.is(unitClass))
}
