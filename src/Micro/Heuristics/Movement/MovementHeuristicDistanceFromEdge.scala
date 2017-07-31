package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Agency.Agent

object MovementHeuristicDistanceFromEdge extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    Math.max(32.0 * 11.0, candidate.distanceFromEdge)
  }
  
}
