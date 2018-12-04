package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromIdealRange extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    val tileDistance =
      candidate.zone.exitDistanceGrid.get(
        candidate.add(
          blueprint.widthTiles.get / 2,
          blueprint.heightTiles.get / 2))

    val output = Math.abs(blueprint.marginPixels.get - 32 * tileDistance)
    output
  }
}
