package Micro.Heuristics.Targeting
import Lifecycle.With
import Micro.Execution.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicPain extends TargetHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
  
    val distanceToReach = Math.max(
      0.0,
      state.unit.pixelsFromEdgeFast(candidate) -
      state.unit.pixelRangeAgainstFromEdge(candidate))
    
    val firingPixel = state.unit.pixelCenter.project(candidate.pixelCenter, distanceToReach)
    
    With.grids.dpsEnemy.get(firingPixel.tileIncluding, state.unit)
  }
  
}
