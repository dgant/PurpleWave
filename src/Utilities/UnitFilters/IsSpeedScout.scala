package Utilities.UnitFilters

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

object IsSpeedScout extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = Protoss.Scout(unit) && Protoss.ScoutSpeed(unit.player)
}
