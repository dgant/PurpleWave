package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState

object Pillage extends Action {
  
  override protected def allowed(state: ActionState): Boolean = {
    state.unit.canAttackThisSecond &&
    state.targets.nonEmpty &&
    (state.toTravel.isEmpty || state.unit.pixelDistanceFast(state.toTravel.get) < 32.0 * 8.0)
  }
  
  override protected def perform(state: ActionState) {
    Potshot.delegate(state)
  }
}