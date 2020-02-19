package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchExpansionTownHall extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && unit.base.exists( ! _.isStartLocation)
}
