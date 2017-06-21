package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState
import Planning.Yolo

object Teamfight extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.canFight &&
    state.unit.battle.exists(_.happening)
  }
  
  override def perform(state: ExecutionState) {
    
    // TODO: When should we continue fighting losing battles?
    // How should we avoid indecision?
    
    if (state.unit.battle.exists(_.estimationGeometric.weGainValue) || Yolo.active) {
      Engage.consider(state)
    }
    else if (state.unit.battle.exists(_.estimationGeometric.weLoseValue)) {
      Disengage.consider(state)
    }
  }
}
