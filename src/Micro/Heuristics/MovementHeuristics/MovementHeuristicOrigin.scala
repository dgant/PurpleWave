package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Tile
import Micro.Intent.Intention

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    val candidateDistance = intent.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate.pixelCenter)
    
    if (candidateDistance == 0) return HeuristicMath.default
    
    val before = intent.unit.travelPixels(intent.unit.tileIncludingCenter,  intent.origin)
    val after  = intent.unit.travelPixels(candidate,                        intent.origin)
    
    (before - after) / candidateDistance
  }
}
