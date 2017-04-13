package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Tile
import Micro.Intent.Intention

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    if (intent.destination.isEmpty) return HeuristicMath.default
  
    val before = intent.unit.travelPixels(intent.unit.tileIncludingCenter,  intent.origin)
    val after  = intent.unit.travelPixels(candidate,               intent.origin)
  
    if (before < With.configuration.combatEvaluationDistanceTiles) return HeuristicMath.default
  
    return HeuristicMath.fromBoolean(after < before)
  }
}
