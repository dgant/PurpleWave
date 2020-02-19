package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchNonStartingTownHall extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft)
}
