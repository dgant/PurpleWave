package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import Micro.Execution.ExecutionState

object Rout extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame &&
    state.unit.pixelCenter.zone != state.origin.zone
  }
  
  override protected def perform(state: ExecutionState): Unit = {
    
    state.toTravel = Some(state.origin)
    Travel.delegate(state)
    
    // TODO: Determine when this is a dumb way to retreat (ie. back through the enemy's army)
  }
  
}
