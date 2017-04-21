package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicMobility extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    if (intent.unit.flying) 1 else With.grids.mobility.get(candidate.tileIncluding) / 10.0
    
  }
  
}
