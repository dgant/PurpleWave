package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicExit extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    candidate.zone.exit
      .map(_.centerPixel.pixelDistanceFast(candidate.pixelCenter))
      .getOrElse(HeuristicMathMultiplicative.default)
  }
}
