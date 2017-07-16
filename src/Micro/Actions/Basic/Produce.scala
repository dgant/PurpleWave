package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Produce extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.unit.trainingQueue.isEmpty
  }
  
  override def perform(state: ActionState) {
    
    if (state.toTrain.isDefined) {
      With.commander.build(state.unit, state.toTrain.get)
      state.intent.toTrain = None //Avoid building repeatedly
    }
    else if (state.toTech.isDefined) {
      With.commander.tech(state.unit, state.toTech.get)
    }
    else if (state.toUpgrade.isDefined) {
      With.commander.upgrade(state.unit, state.toUpgrade.get)
    }
  }
}
