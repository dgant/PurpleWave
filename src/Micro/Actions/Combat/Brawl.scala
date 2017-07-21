package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ActionState
import ProxyBwapi.Races.Zerg

object Brawl extends Action {
  
  // When engaging Zerglings, just attack-move to avoid glitching
  
  override protected def allowed(state: ActionState): Boolean = {
    state.canFight                  &&
    state.unit.canAttackThisSecond  &&
    state.unit.melee                &&
    state.unit.matchups.targets.nonEmpty          &&
    {
      val nearestEnemy = state.unit.matchups.targets.minBy(_.pixelDistanceFast(state.unit))
      nearestEnemy.is(Zerg.Zergling) && nearestEnemy.pixelDistanceFast(state.unit) < 64.0 && nearestEnemy.isBeingViolent
    }
  }
  
  override protected def perform(state: ActionState) {
    Potshot.delegate(state)
  }
}
