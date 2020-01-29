package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Repair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toRepair.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.loaded) {
      unit.transport.foreach(With.commander.unload(_, unit))
    }
    val target = unit.agent.toRepair.get
    if (target.totalHealth < target.unitClass.maxHitPoints) {
      With.commander.repair(unit, target)
    } else {
      unit.agent.toTravel = Some(target.pixelCenter.project(unit.pixelCenter, 48))
      Move.delegate(unit)
    }
  }
}
