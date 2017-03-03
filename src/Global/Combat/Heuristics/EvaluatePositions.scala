package Global.Combat.Heuristics

import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
  def bestPosition(unit:FriendlyUnitInfo, evaluator:EvaluatePosition, searchRange:Int = 3)
      :TilePosition = {
    
    val currentPosition = unit.tilePosition
    val candidates =
      (-searchRange to searchRange).flatten(dy =>
        (-searchRange to searchRange).map(dx => (dx, dy)))
        .map(point => currentPosition.add(point._1, point._2))
        .filter(tile => With.grids.walkability.get(tile) > 0)
  
    if (candidates.isEmpty) {
      //Weird. unit is nowhere near a walkable position
      With.logger.warn("Unit appears to be in a totally unwalkable area")
      return unit.tilePosition
    }
    
    return candidates.maxBy(evaluator.evaluate)
  }
}
