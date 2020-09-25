package Information.Battles.ProcessingStates

import Lifecycle.With

trait BattleProcessState {
  protected def transitionTo(newState: BattleProcessState): Unit = {
    With.battles.setProcessingState(newState)
  }

  def step(): Unit

  def isFinalStep: Boolean = false

  val frameStarted: Int = With.frame
}
