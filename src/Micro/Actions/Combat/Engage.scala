package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ActionState

object Engage extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.targets.nonEmpty
  }
  
  override def perform(state: ActionState) {
    
    state.toTravel = state.unit.battle.map(_.enemy.centroid).orElse(state.toTravel)
    
    Brawl.consider(state)
    BustWallin.consider(state)
    
    //TODO: Don't chase distractions
    Target.delegate(state)
    
    if ( ! state.unit.canAttackThisFrame) {
      Kite.delegate(state)
    }
    Attack.delegate(state)
  }
}
