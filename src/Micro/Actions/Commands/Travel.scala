package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention

object Travel extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMoveThisFrame &&
    intent.destination.isDefined
  }
  
  override def perform(intent: Intention) {
    val pixelToMove = intent.destination.get
    intent.state.movingTo = Some(pixelToMove)
    With.commander.move(intent.unit, pixelToMove)
  }
}
