package Utilities.UnitFilters
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

object IsSpeedlot extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = Protoss.Zealot(unit) && Protoss.ZealotSpeed(unit.player)
}
