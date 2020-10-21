package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Move extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.agent.toTravel.isDefined
  
  override def perform(unit: FriendlyUnitInfo): Unit = With.commander.move(unit)
}
