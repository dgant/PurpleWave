package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import bwapi.TilePosition

object MovementHeuristicKeepMoving extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    HeuristicMath.fromBoolean(intent.unit.tileCenter != candidate)
    
  }
  
}
