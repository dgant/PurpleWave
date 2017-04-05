package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import bwapi.TilePosition
import Utilities.EnrichPosition._

object MovementHeuristicKeepMoving extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    32.0 + intent.unit.pixelDistance(candidate.pixelCenter)
    
  }
  
}
