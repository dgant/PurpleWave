package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Movement.EvaluateMoves
import Micro.Intent.Intention

import Utilities.EnrichPosition._

object Move extends Action {
  
  override def allowed(intent: Intention) = {
    intent.unit.canMove
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.state.movingHeuristically = intent.threats.nonEmpty || intent.targets.nonEmpty
    
    val tileToMove =
      if (intent.state.movingHeuristically)
        EvaluateMoves.best(intent, intent.movementProfile)
      else
        intent.destination.getOrElse(intent.unit.tileIncludingCenter)
    
    intent.state.movement = Some(tileToMove)
    With.commander.move(intent, tileToMove.pixelCenter)
    true
  }
}
