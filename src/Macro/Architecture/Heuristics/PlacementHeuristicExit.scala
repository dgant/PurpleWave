package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicExit extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    candidate.zone.exit
      .map(exit => {
        val distance = candidate.pixelCenter.pixelDistanceFast(exit.centerPixel)
        Math.abs(distance - 32.0 * 6.0)
      })
      .getOrElse(HeuristicMathMultiplicative.default)
  }
}
