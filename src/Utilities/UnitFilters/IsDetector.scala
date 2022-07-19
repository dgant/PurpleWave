package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsDetector extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isDetector
}
