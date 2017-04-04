package Micro.Heuristics.Targeting

import Micro.Heuristics.HeuristicMath
import Micro.Heuristics.UnitHeuristics.UnitHeuristic
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

class WeightedUnitHeuristic(
  val heuristic : UnitHeuristic,
  val weight    : Double) {
  
  def weigh(intent:Intention, candidate:UnitInfo):Double = {
    
    val result =
      if (weight == 0)
        1.0
      else
        Math.pow(HeuristicMath.normalize(heuristic.evaluate(intent, candidate)), weight)
    
    return result
  }
  
}
