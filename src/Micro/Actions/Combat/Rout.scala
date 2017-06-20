package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Rout extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    throw new NotImplementedError
  }
  
  override protected def perform(state: ExecutionState): Unit = {
    throw new NotImplementedError
  }
  
}
