package Micro.Heuristics.General

import Micro.Intent.Intention

class MicroHeuristicWeight[T] (
  val heuristic : MicroHeuristic[T],
  val weight    : Double) {
  
  def weigh(intent:Intention, candidate:T):Double = {
    
    val result =
      if (weight == 0)
        HeuristicMath.default
      else
        Math.pow(HeuristicMath.normalize(heuristic.evaluate(intent, candidate)), weight)
    
    return result
  }
  
}
