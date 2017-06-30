package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicCoversWorkers extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    
    if (building.attackRange.isEmpty) return HeuristicMathMultiplicative.default
    
    candidate.zone.bases
      .map(
        _.harvestingArea.tiles.count(
          _.tileDistanceFast(candidate) * 32.0
          <= building.attackRange.get))
      .sum
  }
}
