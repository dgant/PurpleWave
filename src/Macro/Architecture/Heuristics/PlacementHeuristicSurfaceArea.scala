package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile

object PlacementHeuristicSurfaceArea extends PlacementHeuristic {
  
  override def evaluate(building: BuildingDescriptor, candidate: Tile): Double = {
    
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
            With.grids.walkable.get(borderTile)
            && ! zone.bases.exists(_.townHallArea.contains(borderTile))
            && ! With.architect.exclusions.exists(_.areaExcluded.contains(borderTile))) { //Note that this incorrectly counts margin tiles as non-surface-area
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
