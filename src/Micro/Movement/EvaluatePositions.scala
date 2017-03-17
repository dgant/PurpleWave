package Micro.Movement

import Geometry.Shapes.Circle
import Startup.With
import Micro.Intentions.Intention
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
  def best(intent:Intention, evaluator:EvaluatePosition, searchRange:Int = 3):TilePosition = {
    
    val candidates =
      Circle.points(searchRange)
        .map(intent.unit.tileCenter.add)
        .filter(_.valid)
        .filter(With.grids.walkable.get)
  
    if (candidates.isEmpty) {
      //Weird. unit is nowhere near a walkable position
      return intent.unit.tileCenter
    }
    
    return candidates.maxBy(tile => evaluator.evaluate(intent, tile))
  }
}
