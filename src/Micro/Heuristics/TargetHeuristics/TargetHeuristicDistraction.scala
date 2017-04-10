package Micro.Heuristics.TargetHeuristics

import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDistraction extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
  
    if (intent.destination.isEmpty) return 1.0
  
    HeuristicMath.fromBoolean(
      candidate.travelPixels(intent.destination.get) >
      intent.unit.travelPixels(intent.destination.get))
  }
}
