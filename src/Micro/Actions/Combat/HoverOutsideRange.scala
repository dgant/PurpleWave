package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ExecutionState

object HoverOutsideRange extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame
  }
  
  override def perform(state: ExecutionState) {
    
    state.movementProfile = MovementProfiles.hoverOutsideRange
    Reposition.delegate(state)
  }
}
