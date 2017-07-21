package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Fight
import Micro.Execution.ActionState

object Build extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.intent.toBuild.isDefined &&
    state.intent.toBuildTile.isDefined
  }
  
  override def perform(state: ActionState) {
    
    val buildArea = state.toBuild.get.tileArea.add(state.toBuildTile.get)
    val blockers  = state.unit.matchups.targets.filter(_.tileArea.intersects(buildArea))
    blockers.flatMap(_.friendly).foreach(_.actionState.shove(state.unit))
    if (blockers.exists(_.isEnemy)) {
      Fight.consider(state)
    }
    else {
      With.commander.build(state.unit, state.intent.toBuild.get, state.intent.toBuildTile.get)
    }
  }
}
