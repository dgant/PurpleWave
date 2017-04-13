package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import Mathematics.Pixels.Tile

object MovementHeuristicMobility extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    if (intent.unit.flying) 1 else
      With.grids.mobility.get(candidate) / 10.0
    
  }
  
}
