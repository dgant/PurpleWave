package Micro.Heuristics.TargetHeuristics

import Lifecycle.With
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

object TargetHeuristicFiringPixel extends TargetHeuristic{
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    val firingPixel =
      intent.unit.pixelCenter
        .project(
          candidate.pixelCenter,
          Math.max(0, intent.unit.pixelsFromEdgeFast(candidate) - intent.unit.rangeAgainst(candidate)))
        .tileIncluding
    
    val dpsExposure = With.grids.dpsEnemy.get(firingPixel, intent.unit)
    
    dpsExposure
  }
  
}
