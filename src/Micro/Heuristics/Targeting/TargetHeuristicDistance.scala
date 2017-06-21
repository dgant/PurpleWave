package Micro.Heuristics.Targeting

import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDistance extends TargetHeuristic{
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    Math.max(
      state.unit.pixelRangeAgainstFromEdge(candidate),
      state.unit.pixelsFromEdgeFast(candidate) - state.unit.unitClass.maxAirGroundRange)
  }
}
