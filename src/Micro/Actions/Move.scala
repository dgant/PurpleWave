package Micro.Actions
import Lifecycle.With
import Micro.Heuristics.Movement.EvaluateMoves
import Micro.Intent.Intention

import Utilities.EnrichPosition._

object Move extends Action {
  
  override def perform(intent: Intention): Boolean = {
    
    if ( ! intent.unit.canMove) return false
    
    intent.state.movingHeuristically = intent.threats.nonEmpty || intent.targets.nonEmpty
    
    val tileToMove =
      if (intent.state.movingHeuristically)
        EvaluateMoves.best(intent, intent.movementProfile, 3)
      else
        intent.destination.getOrElse(intent.unit.tileCenter)
    
    With.commander.move(intent, tileToMove.pixelCenter)
    true
  }
  
}
