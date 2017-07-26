package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Commands.MoveHeuristically
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object KiteMove extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    MoveHeuristically.delegate(unit)
  }
}
