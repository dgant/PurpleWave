package Micro.Actions.Terran

import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object GetRepairedBuilding extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.unitClass.isTerran
    && unit.unitClass.isBuilding
    && unit.complete
    && ?(
      unit.isAny(Terran.Bunker, Terran.MissileTurret),
      unit.hitPoints < unit.unitClass.maxHitPoints,
      unit.hitPoints * 2 < unit.unitClass.maxHitPoints))
  
  override def perform(unit: FriendlyUnitInfo): Unit = {

  }
}
