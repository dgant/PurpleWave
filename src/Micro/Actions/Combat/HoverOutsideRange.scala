package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState

object HoverOutsideRange extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.unit.canMoveThisFrame
  }
  
  override def perform(state: ActionState) {
    
    state.movementProfile = MovementProfiles.hoverOutsideRange
    Reposition.delegate(state)
  }
}
