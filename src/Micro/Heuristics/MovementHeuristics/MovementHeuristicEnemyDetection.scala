package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Lifecycle.With
import Micro.Heuristics.General.HeuristicMath
import bwapi.TilePosition

object MovementHeuristicEnemyDetection extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    HeuristicMath.fromBoolean(With.grids.enemyDetection.get(candidate))
    
  }
  
}
