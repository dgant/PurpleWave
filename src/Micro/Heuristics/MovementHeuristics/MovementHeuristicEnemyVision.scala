package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Tile

object MovementHeuristicEnemyVision extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
    
    HeuristicMath.fromBoolean(With.grids.enemyVision.get(candidate))
    
  }
  
}
