package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Startup.With
import bwapi.TilePosition

object TileHeuristicHighGround extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    With.grids.altitudeBonus.get(candidate)
    
  }
  
}
