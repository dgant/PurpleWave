package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Task.ExecutionState

object Produce extends Action {
  
  override def allowed(state:ExecutionState) = {
    state.unit.trainingQueue.isEmpty
  }
  
  override def perform(state:ExecutionState) {
    
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
