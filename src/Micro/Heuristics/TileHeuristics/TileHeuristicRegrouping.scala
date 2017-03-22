package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicRegrouping extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    With.grids.friendlyStrength.get(candidate)  / 100.0
    
  }
  
}
