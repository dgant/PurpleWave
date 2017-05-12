package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.State.ExecutionState
import Planning.Yolo

object Collaborate extends Action {
  
  override def perform(state:ExecutionState) {
    if ( ! Yolo.active) {
      if (retreatArmy(state)) {
        Kite.consider(state)
        Flee.consider(state)
      }
      if (retreatWounded(state)) {
        Flee.consider(state)
      }
      if (retreatWorker(state)) {
        Flee.consider(state)
      }
    }
    
    Charge.consider(state)
  }
  
  private def retreatWorker   (state:ExecutionState):Boolean = workersFlee  (state)  &&   isWorker(state)
  private def retreatWounded  (state:ExecutionState):Boolean = woundedFlee  (state)  &&   isWounded(state)
  private def retreatArmy     (state:ExecutionState):Boolean = fightersFlee (state)  && ! isWorker(state)
  
  private def fightersFlee  (state:ExecutionState)  : Boolean = state.battleEstimation.exists(_.has(Tactics.Movement.Retreat))
  private def woundedFlee   (state:ExecutionState)  : Boolean = state.battleEstimation.exists(_.has(Tactics.Wounded.Flee))
  private def workersFlee   (state:ExecutionState)  : Boolean = state.battleEstimation.exists(_.has(Tactics.Workers.Flee))
  private def isWounded     (state:ExecutionState)  : Boolean = state.unit.wounded
  private def isWorker      (state:ExecutionState)  : Boolean = state.unit.unitClass.isWorker
}
