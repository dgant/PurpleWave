package Micro.Heuristics.Targeting

import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDistance extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    Math.max(
      state.unit.pixelRangeAgainstFromEdge(candidate),
      state.unit.pixelsFromEdgeFast(candidate) - state.unit.unitClass.maxAirGroundRange)
  }
}
