package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class MatchHasAddon(unitClass: UnitClass) extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.addon.exists(_.is(unitClass))
}
