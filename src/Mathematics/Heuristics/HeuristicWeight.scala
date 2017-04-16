package Mathematics.Heuristics

class HeuristicWeight[TContext, TCandidate] (
  val heuristic : Heuristic[TContext, TCandidate],
  val weight    : Double) {
  
  def weigh(context:TContext, candidate:TCandidate):Double = {
    
    val result =
      if (weight == 0)
        HeuristicMath.default
      else
        Math.pow(HeuristicMath.clamp(heuristic.evaluate(context, candidate)), weight)
    
    return result
  }
  
}
