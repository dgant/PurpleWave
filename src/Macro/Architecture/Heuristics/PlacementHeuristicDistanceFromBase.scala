package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {

    var seedTiles = With.geography.ourBases.map(_.heart)
    if (seedTiles.isEmpty) {
      seedTiles = Vector(With.geography.home)
    }

    val distanceTotal = seedTiles
      .map(_.zone.distanceGrid.get(
        candidate.add(
          blueprint.widthTiles.get / 2,
          blueprint.heightTiles.get / 2)))
      .sum

    Math.max(128.0, 32 * distanceTotal)
  }
}
