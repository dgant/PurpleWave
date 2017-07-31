package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

object PlacementHeuristicSurfaceArea extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val zone = candidate.zone
    val dxEnd = blueprint.widthTiles.get + 2
    val dyEnd = blueprint.heightTiles.get + 2
    var walkableTiles = 0
    var dx = 0
    while(dx < dxEnd) {
      var dy = 0
      while(dy < dyEnd) {
        if (dx == 0 || dy == 0 || dx == dxEnd - 1 || dy == dyEnd - 1) {
          val borderTile = candidate.add(dx, dy)
          if (With.architecture.walkable(borderTile) && ! zone.bases.exists(_.townHallArea.contains(borderTile))) {
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
