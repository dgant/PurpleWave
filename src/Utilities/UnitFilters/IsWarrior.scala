package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

object IsWarrior extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && unit.unitClass.orderable
    && unit.unitClass.attacksOrCasts
    && ! unit.unitClass.isWorker
    && (unit.unitClass.canMove || unit.unitClass.isTank)
  )
}
