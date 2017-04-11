package Micro.Heuristics.MovementHeuristics
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import bwapi.TilePosition

object MovementHeuristicDestination extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.destination.isEmpty) return HeuristicMath.default
    
    val before = intent.unit.travelPixels(intent.unit.tileIncludingCenter,  intent.destination.get)
    val after  = intent.unit.travelPixels(candidate,               intent.destination.get)
    
    if (before < With.configuration.combatEvaluationDistanceTiles) return HeuristicMath.default
  
    return HeuristicMath.fromBoolean(after < before - 31.0)
  }
}
