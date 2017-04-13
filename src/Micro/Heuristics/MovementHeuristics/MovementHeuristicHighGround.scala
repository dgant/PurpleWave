package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import Mathematics.Pixels.Tile

object MovementHeuristicHighGround extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    With.grids.altitudeBonus.get(candidate)
    
  }
}
