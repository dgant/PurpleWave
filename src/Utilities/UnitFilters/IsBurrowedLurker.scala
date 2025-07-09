package Utilities.UnitFilters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object IsBurrowedLurker extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.burrowed && Zerg.Lurker(unit)
}
