package Micro.Heuristics.TileHeuristics

import Micro.Heuristics.HeuristicMath
import Micro.Intentions.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object TileHeuristicInRangeOfTarget extends TileHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.toAttack.isEmpty) return 1.0
    
    HeuristicMath.unboolify(intent.toAttack.get.pixelDistance(candidate.pixelCenter) <
      intent.unit.unitClass.hypotenuse +
      intent.toAttack.get.unitClass.hypotenuse +
      intent.unit.unitClass.maxAirGroundRange)
  }
}
