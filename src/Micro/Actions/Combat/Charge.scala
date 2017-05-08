package Micro.Actions.Combat

import Information.Battles.TacticsTypes.Tactics
import Micro.Actions.Action
import Micro.Behaviors.MovementProfiles
import Micro.State.ExecutionState
import Planning.Yolo

object Charge extends Action {
  
  override def allowed(state:ExecutionState): Boolean = {
    state.unit.canMoveThisFrame &&
    (Yolo.active || state.tactics.exists(_.has(Tactics.Movement.Charge)))
  }
  
  override def perform(state:ExecutionState) {
    state.movementProfile = MovementProfiles.charge
  }
}
