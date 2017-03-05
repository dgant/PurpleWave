package Global.Combat.Heuristics

import Geometry.Circle
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
  def bestPosition(unit:FriendlyUnitInfo, evaluator:EvaluatePosition, searchRange:Int = 2):TilePosition = {
    
    val candidates =
      Circle.points(searchRange)
        .map(unit.tileCenter.add)
        .filter(With.grids.walkability.get)
  
    if (candidates.isEmpty) {
      //Weird. unit is nowhere near a walkable position
      With.logger.warn("Unit appears to be in a totally unwalkable area")
      return unit.tileCenter
    }
    
    return candidates.maxBy(evaluator.evaluate)
  }
}
