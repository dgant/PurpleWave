package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Reposition
import Micro.Execution.ExecutionState

object Hover extends Action {
  
  override def allowed(state:ExecutionState): Boolean = (
    state.unit.canMoveThisFrame
    && state.targets.nonEmpty
    && state.unit.pixelRangeMax > 32 * 3.0
  )
  
  override def perform(state:ExecutionState) {
    state.canFight = false
    val threatTargets = state.targets.filter(_.canAttackThisSecond(state.unit))
    if (threatTargets.nonEmpty) {
      state.toAttack = Some(threatTargets.minBy(target => target.pixelDistanceFast(state.unit) - target.pixelRangeAgainstFromEdge(state.unit)))
      Reposition.delegate(state)
    }
    
  }
}
