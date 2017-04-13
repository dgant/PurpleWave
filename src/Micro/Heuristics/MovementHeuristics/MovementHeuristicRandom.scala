package Micro.Heuristics.MovementHeuristics

import Mathematics.Pixels.Tile
import Micro.Intent.Intention
import Utilities.RandomState

object MovementHeuristicRandom extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Tile): Double = {
  
    1.0 + RandomState.random.nextDouble
    
  }
  
}
