package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Task.ExecutionState
import Planning.Yolo

object Collaborate extends Action {
  
  override def perform(state:ExecutionState) {
    if ( ! Yolo.active) {
      if (retreatArmy(state)) {
        Kite.consider(state)
        Flee.consider(state)
      }
      if (state.unit.wounded) {
        Flee.consider(state)
      }
    }
    
    Charge.consider(state)
  }
  
  private def retreatArmy  (state:ExecutionState)  : Boolean = state.battleEstimation.exists(_.weLose)
  private def isWounded    (state:ExecutionState)  : Boolean = state.unit.wounded
}
