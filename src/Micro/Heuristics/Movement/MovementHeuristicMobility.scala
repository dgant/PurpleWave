package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicMobility extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    if (state.unit.flying) 1 else With.grids.mobility.get(candidate.tileIncluding) / 10.0
    
  }
  
}
