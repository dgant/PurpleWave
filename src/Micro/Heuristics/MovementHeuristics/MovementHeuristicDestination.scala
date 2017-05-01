package Micro.Heuristics.MovementHeuristics
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicDestination extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
    
    if (intent.destination.isEmpty) return HeuristicMath.default
    
    val candidateDistance = intent.unit.tileIncludingCenter.pixelCenter.pixelDistanceFast(candidate)
  
    if (candidateDistance <= 0) return HeuristicMath.default
  
    val before = intent.unit.travelPixels(intent.unit.pixelCenter,  intent.destination.get)
    val after  = intent.unit.travelPixels(candidate,                intent.destination.get)
  
    (before - after) / candidateDistance
  }
}
