package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
    
    val zone = intent.unit.tileIncludingCenter.zone
    if (zone.bases.nonEmpty && zone.owner == With.self) return HeuristicMath.default
  
    val candidateDistance = intent.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate) - 24
    
    if (candidateDistance <= 0) return HeuristicMath.default
    
    val before = intent.unit.travelPixels(intent.unit.pixelCenter,  intent.origin)
    val after  = intent.unit.travelPixels(candidate,                intent.origin)
    
    (before - after) / candidateDistance
  }
}
