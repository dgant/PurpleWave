package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Reposition}
import Micro.Behaviors.MovementProfiles
import Micro.Execution.ActionState
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.UnitInfo

object ProtectTheWeak extends Action {
  
  // Protect our workers from harassment. Don't abandon them!
  
  override protected def allowed(state: ActionState): Boolean = {
    state.threatsViolent.isEmpty
  }
  
  override protected def perform(state: ActionState) {
    
    val currentBullies = bullies(state)
    
    if (currentBullies.nonEmpty) {
  
      state.canCower = false
      state.toAttack = EvaluateTargets.best(state, currentBullies)
      state.movementProfile = MovementProfiles.safelyAttackTarget
  
      if (state.unit.readyForAttackOrder) {
        Attack.delegate(state)
      }
      else {
        // Avoid taking damage while we defen
        Reposition.delegate(state)
      }
    }
  }
  
  private def bullies(state: ActionState): Iterable[UnitInfo] = {
    state.neighbors
      .filter(neighbor =>
        neighbor.unitClass.isWorker ||
        (
          neighbor.unitClass.isBuilding &&
          (neighbor.wounded || neighbor.unitClass.canAttack))
        )
      .flatMap(neighbor => state.targets.filter(_.isBeingViolentTo(neighbor)))
  }
}
