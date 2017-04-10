package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import bwapi.TilePosition

object MovementHeuristicHighGround extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    With.grids.altitudeBonus.get(candidate)
    
  }
}
