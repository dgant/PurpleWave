package Micro.Actions.Commands

import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Move extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.agent.toTravel.isDefined
  
  override def perform(unit: FriendlyUnitInfo): Unit = Commander.move(unit)
}
