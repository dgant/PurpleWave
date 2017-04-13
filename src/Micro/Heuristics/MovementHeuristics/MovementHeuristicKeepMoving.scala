package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Tile
import Micro.Intent.Intention

object MovementHeuristicKeepMoving extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    HeuristicMath.fromBoolean(intent.unit.tileIncludingCenter != candidate)
    
  }
  
}
