package Micro.Heuristics.TileHeuristics

import Micro.Heuristics.HeuristicMath
import Micro.Intentions.Intention
import bwapi.TilePosition

object TileHeuristicDestinationHere extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.destination.isEmpty) return 1.0
  
    val before = intent.unit.travelPixels(intent.unit.tileCenter,  intent.destination.get)
    val after  = intent.unit.travelPixels(candidate,               intent.destination.get)
    
    return HeuristicMath.unboolify(before < after)
  }
  
}
