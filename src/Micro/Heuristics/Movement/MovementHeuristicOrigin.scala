package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicOrigin extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
    
    val zone = intent.unit.tileIncludingCenter.zone
    if (zone.bases.nonEmpty && zone.owner == With.self) return HeuristicMathMultiplicative.default
  
    val candidateDistance = intent.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
    
    if (candidateDistance <= 0) return HeuristicMathMultiplicative.default
    
    val before = intent.unit.pixelDistanceTravelling(intent.unit.pixelCenter,  intent.origin)
    val after  = intent.unit.pixelDistanceTravelling(candidate,                intent.origin)
    
    (before - after) / candidateDistance
  }
}
