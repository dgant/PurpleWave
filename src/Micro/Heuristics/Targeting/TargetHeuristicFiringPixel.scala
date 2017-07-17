package Micro.Heuristics.Targeting

import Lifecycle.With
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicFiringPixel extends TargetHeuristic{
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    val firingPixel =
      state.unit.pixelCenter
        .project(
          candidate.pixelCenter,
          Math.max(0, state.unit.pixelsFromEdgeFast(candidate) - state.unit.pixelRangeAgainstFromEdge(candidate)))
        .tileIncluding
    
    val dpfExposure = With.grids.dpfEnemy.get(firingPixel, state.unit)
    
    dpfExposure
  }
}
