package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Movement.EvaluateTiles
import Micro.Intent.Intention

object Move extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMoveThisFrame
  }
  
  override def perform(intent: Intention): Boolean = {
    val moveHeuristically = intent.threats.nonEmpty || intent.targets.nonEmpty
    val tileToMove =
      if (moveHeuristically)
        EvaluateTiles.best(intent, intent.movementProfile)
      else
        intent.destination.getOrElse(intent.unit.tileIncludingCenter)
    
    intent.state.movement = Some(tileToMove)
    if (moveHeuristically) {
      intent.state.movedHeuristicallyFrame = With.frame
    }
    
    With.commander.move(intent, tileToMove.pixelCenter)
    true
  }
}
