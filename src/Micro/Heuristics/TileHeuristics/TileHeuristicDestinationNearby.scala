package Micro.Heuristics.TileHeuristics
import Micro.Intentions.Intention
import bwapi.TilePosition

object TileHeuristicDestinationNearby extends TileHeuristic {
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.destination.isEmpty) return 1.0
    
    val before = intent.unit.travelPixels(intent.unit.tileCenter,  intent.destination.get)
    val after  = intent.unit.travelPixels(candidate,               intent.destination.get)
    
    val threshold = 32.0 * 8.0
    if (before < threshold || after < threshold) return 1.0
  
    return before/after
  }
}
