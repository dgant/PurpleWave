package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEntrance extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    val output = 32 * Math.max(
      1.0,
      candidate.zone.exitDistanceGrid.get(
        candidate.add(
          blueprint.widthTiles.get / 2,
          blueprint.heightTiles.get / 2)))
    output
  }
}
