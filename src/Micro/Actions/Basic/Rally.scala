package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Rally extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    ! unit.hasSetRallyPoint           &&
    unit.unitClass.isBuilding         &&
    unit.unitClass.trainsGroundUnits  &&
    unit.canDoAnythingThisFrame
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.unitClass.isTownHall) {
      val minerals = unit.pixelCenter.zone.bases.flatMap(_.minerals)
      if (minerals.nonEmpty) {
        val mineral = minerals.minBy(_.pixelDistanceFast(unit))
        With.commander.rally(unit, mineral.pixelCenter) // Rallying onto the mineral itself doesn't esem to work
      }
    }
    else {
      unit.pixelCenter.zone.exit.foreach(exit => With.commander.rally(unit, exit.centerPixel))
    }
  }
}
