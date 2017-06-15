package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Behaviors.MovementProfiles
import Micro.Task.ExecutionState
import Planning.Yolo

object Charge extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.unit.canMoveThisFrame &&
    (Yolo.active || state.battleEstimation.exists(_.weGainValue))
  }
  
  override def perform(state:ExecutionState) {
    state.movementProfile = MovementProfiles.charge
  }
}
