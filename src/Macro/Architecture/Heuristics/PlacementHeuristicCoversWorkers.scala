package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicCoversWorkers extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    if (blueprint.distancePixels.isEmpty) return HeuristicMathMultiplicative.default
    
    candidate.zone.bases
      .map(
        _.harvestingArea.tiles.count(
          _.tileDistanceFast(candidate) * 32.0
          <= blueprint.distancePixels.get))
      .sum
  }
}
