package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Lifecycle.With
import bwapi.TilePosition

object TileHeuristicMobility extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.unit.flying) 1 else
      With.grids.mobility.get(candidate) / 10.0
    
  }
  
}
