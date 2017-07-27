package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackMove extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.action.toTravel.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pixelToMove = unit.action.toTravel.get
    unit.action.movingTo = Some(pixelToMove)
    With.commander.attackMove(unit, pixelToMove)
  }
}
