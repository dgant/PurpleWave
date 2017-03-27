package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicStrength extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
    
    val max = 2.0
    val min = 1.0/max
    
    val ourStrength   = With.grids.friendlyStrength.get(candidate)
    val enemyStrength = With.grids.enemyStrength.get(candidate)
    
    if (enemyStrength == 0) return if (ourStrength == 0) 1.0 else max
    if (ourStrength == 0)   return min
    else                    return Math.min(max, Math.max(min, ourStrength / enemyStrength))
  }
}
