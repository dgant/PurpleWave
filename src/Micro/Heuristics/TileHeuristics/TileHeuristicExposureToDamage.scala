package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicExposureToDamage extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    With.grids.enemyStrength.get(candidate) / 100.0
    
  }
  
}
