package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicCoversWorkers extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    candidate.zone.bases
      .map(
        _.harvestingArea.tiles.count(
          _.tileDistanceFast(candidate) * 32.0
          <= blueprint.preferredDistanceFromExit.get))
      .sum
  }
}
