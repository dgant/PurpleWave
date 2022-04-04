package Utilities.UnitFilters

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object IsRecruitableForCombat extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = (
    unit.unitClass.orderable
    && ! Zerg.Larva(unit)
    && unit.remainingCompletionFrames < 48
    && (unit.unitClass.canMove || unit.unitClass.isTank || (unit.unitClass.isBuilding && unit.flying)))
}
