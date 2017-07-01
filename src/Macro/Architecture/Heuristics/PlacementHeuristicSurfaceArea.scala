package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicSurfaceArea extends PlacementHeuristic {
  
  override def evaluate(building: Blueprint, candidate: Tile): Double = {
    
    val zone = candidate.zone
    val dxEnd = building.width + 2
    val dyEnd = building.height + 2
    var walkableTiles = 0
    var dx = 0
    while(dx < dxEnd) {
      var dy = 0
      while(dy < dyEnd) {
        if (dx == 0 || dy == 0 || dx == dxEnd - 1 || dy == dyEnd - 1) {
          val borderTile = candidate.add(dx, dy)
          if (
               ! zone.bases.exists(_.townHallArea.contains(borderTile))
            && ! With.architecture.walkable(borderTile)) {
            walkableTiles += 1
          }
        }
        dy += 1
      }
      dx += 1
    }
    
    walkableTiles
  }
}
