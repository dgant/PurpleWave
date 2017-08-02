package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchHasAddon(unitClass: UnitClass) extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.addon.exists(_.is(unitClass))
}
