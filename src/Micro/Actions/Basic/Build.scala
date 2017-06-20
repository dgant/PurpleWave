package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Build extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.intent.toBuild.isDefined &&
    state.intent.toBuildTile.isDefined
  }
  
  override def perform(state: ExecutionState) {
    With.commander.build(state.unit, state.intent.toBuild.get, state.intent.toBuildTile.get)
  }
}
