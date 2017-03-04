package Global.Combat.Heuristics

import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.TilePosition

object EvaluatePositions {
  
  def bestPosition(unit:FriendlyUnitInfo, evaluator:EvaluatePosition, searchRange:Int = 3):TilePosition = {
    
    val candidates =
      (-searchRange to searchRange).flatten(dy =>
        (-searchRange to searchRange).map(dx => (dx, dy)))
        .map(point => unit.tileCenter.add(point._1, point._2))
        .filter(With.grids.walkability.get)
  
    if (candidates.isEmpty) {
      //Weird. unit is nowhere near a walkable position
      With.logger.warn("Unit appears to be in a totally unwalkable area")
      return unit.tileCenter
    }
    
    return candidates.maxBy(evaluator.evaluate)
  }
}
