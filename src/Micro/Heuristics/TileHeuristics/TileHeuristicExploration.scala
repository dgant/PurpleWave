package Micro.Heuristics.TileHeuristics

import Micro.Heuristics.HeuristicMath
import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicExploration extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    
    HeuristicMath.unboolify(With.grids.friendlyVision.get(candidate))
    
  }
  
}
