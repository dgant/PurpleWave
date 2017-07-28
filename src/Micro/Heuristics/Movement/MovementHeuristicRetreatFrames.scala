package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Agency.Agent

object MovementHeuristicRetreatFrames extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    state.unit.matchups.ifAt(candidate).framesToRetreatDiffused
    
  }
}
