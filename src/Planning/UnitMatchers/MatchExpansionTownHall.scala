package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchExpansionTownHall extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && unit.base.exists( ! _.isStartLocation)
}
