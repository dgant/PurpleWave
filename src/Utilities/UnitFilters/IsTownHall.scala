package Utilities.UnitFilters
import ProxyBwapi.UnitInfo.UnitInfo

object IsTownHall extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isTownHall
}
