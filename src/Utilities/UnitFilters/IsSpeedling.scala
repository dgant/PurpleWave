package Utilities.UnitFilters
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object IsSpeedling extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = Zerg.Zergling(unit) && Zerg.ZerglingSpeed(unit.player)
}
