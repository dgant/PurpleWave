package Micro.Actions.Commands

import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Travel extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove
  override def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.intent.canSneak) {
      MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    } else {
      Commander.move(unit)
    }
  }
}
