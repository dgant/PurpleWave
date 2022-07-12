package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsLandedBuilding extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isBuilding && ! unit.flying
}
