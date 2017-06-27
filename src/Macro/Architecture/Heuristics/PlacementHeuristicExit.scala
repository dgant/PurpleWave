package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicExit extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    candidate.zone.exit
      .map(exit => {
        val distance = candidate.pixelCenter.pixelDistanceFast(exit.centerPixel)
        
        exit.centerPixel.midpoint(candidate.zone.centroid.pixelCenter).pixelDistanceFast(candidate.pixelCenter)
      })
      .getOrElse(HeuristicMathMultiplicative.default)
  }
}
