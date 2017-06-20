package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Reposition}
import Micro.Execution.ExecutionState

object Engage extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    state.canFight &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ExecutionState) {
    
    // TODO: Avoid chasing distractions (make PURSUE the judge of that).
    
    state.toTravel = state.unit.battle.map(_.enemy.centroid).orElse(state.toTravel)
    
    Brawl.consider(state)
    Target.delegate(state)
    if (state.unit.canAttackThisFrame) {
      Attack.delegate(state)
    }
    else {
      Reposition.delegate(state)
    }
  }
}
