package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsWorker extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.unitClass.isWorker
}
