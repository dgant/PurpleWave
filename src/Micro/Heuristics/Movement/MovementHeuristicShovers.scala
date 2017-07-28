package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Agency.Agent
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object MovementHeuristicShovers extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    if (state.unit.flying) return HeuristicMathMultiplicative.default
    
    state.shovers.map(evaluateShover(state, candidate, _)).sum
  }
  
  def evaluateShover(
                      state     : Agent,
                      candidate : Pixel,
                      shover    : FriendlyUnitInfo): Double = {
    
    val margin = 2.0 * state.unit.unitClass.radialHypotenuse + shover.unitClass.radialHypotenuse
    val shoveFrom = shover.agent.movingTo.map(shover.pixelCenter.project(_, margin)).getOrElse(shover.pixelCenter)
    
    Math.min(margin, shoveFrom.pixelDistanceFast(candidate))
  }
}
