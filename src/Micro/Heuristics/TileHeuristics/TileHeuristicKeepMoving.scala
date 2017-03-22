package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import bwapi.TilePosition
import Utilities.EnrichPosition._

object TileHeuristicKeepMoving extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    intent.unit.pixelDistance(candidate.pixelCenter)
    
  }
  
}
