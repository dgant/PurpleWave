package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicEnemy extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
  
    if (With.geography.enemyBases.isEmpty)
      candidate.groundPixels(With.intelligence.mostBaselikeEnemyTile)
    else
      With.geography.enemyBases.map(_.heart.groundPixels(candidate)).min
  }
}
