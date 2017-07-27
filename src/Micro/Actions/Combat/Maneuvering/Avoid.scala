package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Commands.MoveHeuristically
import Micro.Behaviors.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    unit.action.movementProfile = MovementProfiles.avoid
    MoveHeuristically.delegate(unit)
  }
}
