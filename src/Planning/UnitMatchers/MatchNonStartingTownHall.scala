package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNonStartingTownHall extends UnitMatcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft)
}
