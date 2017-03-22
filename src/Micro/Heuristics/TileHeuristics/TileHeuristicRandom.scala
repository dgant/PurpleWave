package Micro.Heuristics.TileHeuristics

import Micro.Intentions.Intention
import Utilities.RandomState
import bwapi.TilePosition

object TileHeuristicRandom extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    RandomState.random.nextDouble
    
  }
  
}
