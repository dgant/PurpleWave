package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromBase extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {

    var seedTiles = (candidate.zone.bases.view ++ With.geography.ourBases.view).map(_.heart)
    if (seedTiles.isEmpty) {
      seedTiles = Vector(With.geography.home).view
    }

    val distanceTotal = seedTiles
      .map(_.zone.distanceGrid.get(
        candidate.add(
          blueprint.widthTiles.get / 2,
          blueprint.heightTiles.get / 2)))
      .sum

    val output = 6 + distanceTotal
    output
  }
}
