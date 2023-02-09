package Utilities.UnitFilters

import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

object IsSpeedVulture extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = Terran.Vulture(unit) && Terran.VultureSpeed(unit.player)
}
