package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import Utilities.EnrichPosition._
import bwapi.TilePosition

object MovementHeuristicInRangeOfTarget extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.toAttack.isEmpty) return 1.0
    
    HeuristicMath.fromBoolean(
      intent.toAttack.get.pixelDistanceSquared(candidate.pixelCenter) <
      Math.pow(
        intent.unit.unitClass.radialHypotenuse +
        intent.toAttack.get.unitClass.radialHypotenuse +
        intent.unit.unitClass.maxAirGroundRange,
        2))
  }
}
