package Micro.Actions.Commands

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Movement.EvaluatePixels
import Micro.Intent.Intention

object Reposition extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMoveThisFrame
  }
  
  override def perform(intent: Intention) {
    
    val pixelToMove = EvaluatePixels.best(intent, intent.movementProfile)
    intent.state.movingTo = Some(pixelToMove)
    intent.state.movedHeuristicallyFrame = With.frame
    With.commander.move(intent, pixelToMove)
  }
}
