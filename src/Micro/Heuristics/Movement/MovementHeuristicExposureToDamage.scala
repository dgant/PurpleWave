package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.State.ExecutionState

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    With.grids.dpsEnemy.get(candidate.tileIncluding, state.unit)
    
  }
}
