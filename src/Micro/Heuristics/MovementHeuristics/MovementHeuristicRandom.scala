package Micro.Heuristics.MovementHeuristics

import Micro.Intent.Intention
import Utilities.RandomState
import bwapi.TilePosition

object MovementHeuristicRandom extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    1.0 + RandomState.random.nextDouble
    
  }
  
}
