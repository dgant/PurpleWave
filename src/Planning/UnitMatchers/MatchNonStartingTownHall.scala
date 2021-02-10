package Planning.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

object MatchNonStartingTownHall extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall && ! unit.base.exists(b => b.isStartLocation && b.townHallTile == unit.tileTopLeft)
}
