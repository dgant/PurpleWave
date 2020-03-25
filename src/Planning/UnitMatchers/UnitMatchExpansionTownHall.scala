package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchExpansionTownHall extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && unit.base.exists( ! _.isStartLocation)
}
