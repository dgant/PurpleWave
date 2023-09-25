package Micro.Actions.Basic

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Repair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toRepair.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.loaded) {
      unit.transport.foreach(Commander.unload(_, unit))
    }
    val target = unit.agent.toRepair.get
    if (target.totalHealth < target.unitClass.maxHitPoints) {
      Commander.repair(unit, target)
    } else {
      unit.agent.decision.set(target.pixel.project(unit.pixel, 48))
      Commander.move(unit)
    }
  }
}
