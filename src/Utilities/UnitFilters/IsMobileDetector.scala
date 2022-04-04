package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsMobileDetector extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isDetector && unit.canMove
}
