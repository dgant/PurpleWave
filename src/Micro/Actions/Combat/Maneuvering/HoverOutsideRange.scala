package Micro.Actions.Combat.Maneuvering

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Behaviors.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object HoverOutsideRange extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    unit.action.movementProfile = MovementProfiles.safelyAttackTarget
    Reposition.delegate(unit)
  }
}
