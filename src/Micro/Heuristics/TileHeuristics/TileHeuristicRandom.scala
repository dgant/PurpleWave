package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Utilities.RandomState
import bwapi.TilePosition

object TileHeuristicRandom extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    1.0 + RandomState.random.nextDouble
    
  }
  
}
