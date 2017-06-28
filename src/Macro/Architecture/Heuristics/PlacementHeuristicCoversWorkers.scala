package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicCoversWorkers extends PlacementHeuristic {
  
  override def evaluate(building: BuildingDescriptor, candidate: Tile): Double = {
    
    if (building.attackRange.isEmpty) return HeuristicMathMultiplicative.default
    
    candidate.zone.bases
      .map(
        _.harvestingArea.tiles.count(
          _.tileDistanceFast(candidate) * 32.0
          <= building.attackRange.get))
      .sum
  }
}
