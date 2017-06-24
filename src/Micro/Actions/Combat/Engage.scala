package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Reposition}
import Micro.Execution.ActionState

object Engage extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ActionState) {
    
    // TODO: Avoid chasing distractions (make PURSUE the judge of that).
    
    state.toTravel = state.unit.battle.map(_.enemy.centroid).orElse(state.toTravel)
    
    Brawl.consider(state)
    BustWallin.consider(state)
    Target.delegate(state)
    if (state.unit.canAttackThisFrame) {
      Attack.delegate(state)
    }
    else {
      Reposition.delegate(state)
    }
  }
}
