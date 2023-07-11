package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsWarrior extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = unit.aliveAndComplete && unit.unitClass.isWarrior
}
