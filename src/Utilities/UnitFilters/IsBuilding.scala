package Utilities.UnitFilters
import ProxyBwapi.UnitInfo.UnitInfo

object IsBuilding extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding
}
