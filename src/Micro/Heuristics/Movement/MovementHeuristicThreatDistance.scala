package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicThreatDistance extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    if (intent.threats.isEmpty) return HeuristicMath.default
    
    intent.threats.map(threat => threat.pixelDistanceFast(candidate) - threat.pixelRangeAgainst(intent.unit)).min
  }
}
