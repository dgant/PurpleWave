package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicStrength extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    Math.max(1.0/100.0, Math.min(100.0, With.grids.friendlyStrength.get(candidate) / With.grids.enemyStrengthStrength.get(candidate)))
    
  }
  
}
