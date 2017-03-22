package Micro.Heuristics.Movement

import Geometry.Shapes.Circle
import Startup.With
import Micro.Intentions.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
  def best(intent:Intention, profile:MovementProfile, searchRange:Int = 4):TilePosition = {
    
    With.movementHeuristicViews.reset(intent.unit)
    
    val candidates =
      Circle.points(searchRange)
        .map(intent.unit.tileCenter.add)
        .filter(_.valid)
        .filter(With.grids.walkable.get)
  
    if (candidates.isEmpty) {
      //Weird. unit is nowhere near a walkable position
      return intent.unit.tileCenter
    }
    
    return candidates.maxBy(tile => profile.evaluate(intent, tile))
  }
}
