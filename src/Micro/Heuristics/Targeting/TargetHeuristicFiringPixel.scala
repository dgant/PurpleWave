package Micro.Heuristics.Targeting

import Lifecycle.With
import Micro.Task.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicFiringPixel extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    val firingPixel =
      state.unit.pixelCenter
        .project(
          candidate.pixelCenter,
          Math.max(0, state.unit.pixelsFromEdgeFast(candidate) - state.unit.pixelRangeAgainstFromEdge(candidate)))
        .tileIncluding
    
    val dpsExposure = With.grids.dpsEnemy.get(firingPixel, state.unit)
    
    dpsExposure
  }
}
