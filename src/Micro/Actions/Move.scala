package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Movement.EvaluatePixels
import Micro.Intent.Intention

object Move extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMoveThisFrame
  }
  
  override def perform(intent: Intention): Boolean = {
    val moveHeuristically = intent.threats.nonEmpty || intent.targets.nonEmpty
    val pixelToMove =
      if (moveHeuristically)
        EvaluatePixels.best(intent, intent.movementProfile)
      else
        intent.destination.getOrElse(intent.unit.pixelCenter)
    
    intent.state.movingTo = Some(pixelToMove)
    if (moveHeuristically) {
      intent.state.movedHeuristicallyFrame = With.frame
    }
    
    With.commander.move(intent, pixelToMove)
    true
  }
}
