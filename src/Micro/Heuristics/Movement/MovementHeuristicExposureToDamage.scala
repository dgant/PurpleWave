package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Task.ExecutionState

object MovementHeuristicExposureToDamage extends MovementHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: Pixel): Double = {
  
    if (state.threats.forall(_.melee)) return MovementHeuristicThreatDistance.evaluate(state, candidate)
    
    With.grids.dpsEnemy.get(candidate.tileIncluding, state.unit)
    
  }
}
