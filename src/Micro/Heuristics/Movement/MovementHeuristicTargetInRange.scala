package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

object MovementHeuristicTargetInRange extends MovementHeuristic {
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    if (intent.toAttack.isEmpty) return HeuristicMath.default
    
    HeuristicMath.fromBoolean(
      intent.toAttack.get.pixelDistanceSquared(candidate) <
      Math.pow(
        intent.unit.pixelRangeAgainst(intent.toAttack.get) +
        intent.unit.unitClass.radialHypotenuse +
        intent.toAttack.get.unitClass.radialHypotenuse,
        2))
  }
}
