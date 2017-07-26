package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicVpfDealing extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    state.unit.matchups.ifAt(candidate).vpfDealingDiffused
    
  }
}
