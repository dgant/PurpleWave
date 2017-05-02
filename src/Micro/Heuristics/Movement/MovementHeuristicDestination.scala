package Micro.Heuristics.Movement
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicDestination extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
    
    if (intent.destination.isEmpty) return HeuristicMathMultiplicative.default
    
    val candidateDistance = intent.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
  
    if (candidateDistance <= 0) return HeuristicMathMultiplicative.default
  
    val before = intent.unit.pixelDistanceTravelling(intent.unit.pixelCenter,  intent.destination.get)
    val after  = intent.unit.pixelDistanceTravelling(candidate,                intent.destination.get)
  
    (before - after) / candidateDistance
  }
}
