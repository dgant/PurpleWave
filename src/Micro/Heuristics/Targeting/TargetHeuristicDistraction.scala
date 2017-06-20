package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Execution.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDistraction extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
  
    if (state.toTravel.isEmpty) return 1.0
  
    HeuristicMathMultiplicative.fromBoolean(
      candidate.pixelDistanceTravelling(state.toTravel.get) >
      state.unit.pixelDistanceTravelling(state.toTravel.get))
  }
}
