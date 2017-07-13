package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.{Disengage, Potshot}
import Micro.Execution.ActionState

object Gather extends Action {
  
  override def allowed(state: ActionState): Boolean = {
    state.toGather.isDefined
  }
  
  override def perform(state: ActionState) {
  
    Potshot.consider(state)
    
    if ( ! state.toGather.exists(_.pixelCenter.zone == state.unit.pixelCenter.zone)) {
      if (state.threatsViolent.nonEmpty) {
        Disengage.consider(state)
      }
    }
    
    if (stillReady(state)) {
      With.commander.gather(state.unit, state.toGather.get)
    }
  }
}
