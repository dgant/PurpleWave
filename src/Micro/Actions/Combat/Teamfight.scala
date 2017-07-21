package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState
import Planning.Yolo

object Teamfight extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight
  }
  
  override def perform(state: ActionState) {
    
    // TODO: When should we continue fighting losing battles?
    // How should we avoid indecision?
    
    if (state.threats.isEmpty) {
      Engage.consider(state)
    }
    else if (state.unit.battle.exists(_.estimationGeometricOffense.weGainValue) || Yolo.active) {
      Engage.consider(state)
    }
    else if (state.unit.battle.exists(_.estimationGeometricOffense.weLoseValue)) {
      Disengage.consider(state)
    }
    Engage.consider(state)
  }
}
