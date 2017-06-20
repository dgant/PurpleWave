package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Basic.Gather
import Micro.Actions.Commands.Attack
import Micro.Execution.ExecutionState
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.UnitInfo

object ProtectTheWeak extends Action {
  
  // Protect our workers from harassment. Don't abandon them!
  
  override protected def allowed(state: ExecutionState): Boolean = {
    true
  }
  
  override protected def perform(state: ExecutionState) {
    val currentBullies = bullies(state)
    
    if (currentBullies.nonEmpty) {
      state.toAttack = EvaluateTargets.best(state, currentBullies)
      Attack.delegate(state)
      // TODO: Keep repositioning while hitting bullies!
    }
  }
  
  private def bullies(state: ExecutionState): Iterable[UnitInfo] = {
    state.neighbors
      .filter(neighbor => neighbor.executionState.lastAction.exists(innocentActions.contains))
      .flatMap(neighbor => state.targets.filter(_.isBeingViolentTo(neighbor)))
  }
  
  private val innocentActions = Array(Gather)
}
