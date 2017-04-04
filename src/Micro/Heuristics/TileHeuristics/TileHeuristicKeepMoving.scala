package Micro.Heuristics.TileHeuristics

import Micro.Intent.Intention
import bwapi.TilePosition
import Utilities.EnrichPosition._

object TileHeuristicKeepMoving extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    32.0 + intent.unit.pixelDistance(candidate.pixelCenter)
    
  }
  
}
