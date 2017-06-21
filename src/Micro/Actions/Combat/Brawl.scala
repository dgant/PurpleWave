package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Zerg

object Brawl extends Action {
  
  // When engaging Zerglings, just attack-move to avoid glitching
  
  override protected def allowed(state: ActionState): Boolean = {
    state.canFight &&
    state.unit.canAttackThisSecond &&
    state.unit.melee &&
    state.targets.nonEmpty &&
    state.targets.minBy(_.pixelDistanceFast(state.unit)).is(Zerg.Zergling)
  }
  
  override protected def perform(state: ActionState) {
    val target = state.unit.battle
      .map(_.enemy.vanguard)
      .getOrElse(state.targets.minBy(_.pixelDistanceFast(state.unit)).pixelCenter)
        
    With.commander.attackMove(state.unit, target)
  }
}
