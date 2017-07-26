package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Execution.ActionState

object MovementHeuristicRetreatFrames extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    state.unit.matchups.ifAt(candidate).framesToRetreatDiffused
    
  }
}
