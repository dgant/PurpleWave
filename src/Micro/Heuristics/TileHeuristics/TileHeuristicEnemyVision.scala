package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Micro.Heuristics.HeuristicMath
import Startup.With
import bwapi.TilePosition

object TileHeuristicEnemyVision extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    
    HeuristicMath.unboolify(With.grids.enemyVision.get(candidate))
    
  }
  
}
