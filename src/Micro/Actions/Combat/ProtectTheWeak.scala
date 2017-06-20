package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Basic.Gather
import Micro.Actions.Commands.{Attack, Reposition}
import Micro.Behaviors.MovementProfiles
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
      state.movementProfile = MovementProfiles.safelyAttackTarget
  
      if (state.unit.canAttackThisFrame) {
        Attack.delegate(state)
      }
      else {
        // Avoid taking damage while we defend our workers
        Reposition.delegate(state)
      }
    }
  }
  
  private def bullies(state: ExecutionState): Iterable[UnitInfo] = {
    state.neighbors
      .filter(neighbor => neighbor.executionState.lastAction.exists(innocentActions.contains))
      .flatMap(neighbor => state.targets.filter(_.isBeingViolentTo(neighbor)))
  }
  
  private val innocentActions = Array(Gather)
}
