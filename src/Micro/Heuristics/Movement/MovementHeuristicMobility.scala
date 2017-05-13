package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Task.ExecutionState

object MovementHeuristicMobility extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.unit.flying) 1 else With.grids.mobility.get(candidate.tileIncluding) / 10.0
    
  }
  
}
