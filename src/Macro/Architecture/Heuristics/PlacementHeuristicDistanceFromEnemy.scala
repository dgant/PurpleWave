package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicDistanceFromEnemy extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {

    var seedTiles = With.geography.enemyBases.map(_.heart)
    if (seedTiles.isEmpty) {
      With.geography.startBases.filterNot(_.owner.isUs)
    }
    if (seedTiles.isEmpty) {
      seedTiles = Vector(With.intelligence.mostBaselikeEnemyTile)
    }

    val distanceMin = seedTiles
      .map(_.zone.distanceGrid.get(
        candidate.add(
          blueprint.widthTiles.get / 2,
          blueprint.heightTiles.get / 2)))
      .min
    
    val output = 6 + distanceMin
    output
  }
}
