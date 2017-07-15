package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Sneak extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.cloaked &&
    state.threats.nonEmpty &&
    state.enemies.exists(e => e.unitClass.isDetector && e.aliveAndComplete)
  }
  
  override protected def perform(state: ActionState) {
    Potshot.delegate(state)
    Retreat.delegate(state)
  }
}
