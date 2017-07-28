package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Agent

object MovementHeuristicMobility extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    if (state.unit.flying) 1 else With.grids.mobility.get(candidate.tileIncluding) / 10.0
    
  }
  
}
