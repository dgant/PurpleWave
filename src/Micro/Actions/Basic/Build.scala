package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState

object Build extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.intent.toBuild.isDefined &&
    state.intent.toBuildTile.isDefined
  }
  
  override def perform(state: ActionState) {
    With.commander.build(state.unit, state.intent.toBuild.get, state.intent.toBuildTile.get)
  }
}
