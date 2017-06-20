package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Execution.ExecutionState

object Engage extends Action {
  
  override def allowed(state: ExecutionState): Boolean = {
    // TODO: Are these the right conditions?
    state.canFight              &&
    state.toAttack.isEmpty      &&
    state.unit.canMoveThisFrame &&
    state.targets.nonEmpty      &&
    {
      val zone = state.unit.pixelCenter.zone
      ! zone.owner.isNeutral || state.toTravel.exists(_.zone == zone)
    }
  }
  
  override def perform(state: ExecutionState) {
    Brawl.consider(state)
    Target.delegate(state)
    Attack.delegate(state)
    // TODO: How do we kite?
  }
}
