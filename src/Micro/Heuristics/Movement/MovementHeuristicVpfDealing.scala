package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Agency.Agent

object MovementHeuristicVpfDealing extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    240 * state.unit.matchups.ifAt(candidate).vpfDealingDiffused
    
  }
}
