package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ActionState
import Micro.Heuristics.Targeting.EvaluateTargets

object Build extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.intent.toBuild.isDefined &&
    state.intent.toBuildTile.isDefined
  }
  
  override def perform(state: ActionState) {
    
    val buildArea = state.toBuild.get.tileArea.add(state.toBuildTile.get)
    val blockers  = state.targets.filter(_.tileArea.intersects(buildArea))
    if (blockers.nonEmpty) {
      state.toAttack = EvaluateTargets.best(state, blockers)
      Attack.delegate(state)
    }
    else {
      With.commander.build(state.unit, state.intent.toBuild.get, state.intent.toBuildTile.get)
    }
  }
}
