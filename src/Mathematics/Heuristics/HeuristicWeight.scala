package Mathematics.Heuristics

class HeuristicWeight[TContext, TCandidate] (
  val heuristic : Heuristic[TContext, TCandidate],
  val weight    : Double) {
  
  def weighMultiplicatively(context:TContext, candidate:TCandidate):Double = {
    val result =
      if (weight == 0)
        HeuristicMathMultiplicative.default
      else
        Math.pow(HeuristicMathMultiplicative.clamp(heuristic.evaluate(context, candidate)), weight)
    
    result
  }
  
  def weighAdditively(context:TContext, candidate:TCandidate):Double = {
    val result =
      if (weight == 0)
        HeuristicMathAdditive.default
      else
        HeuristicMathAdditive.clamp(heuristic.evaluate(context, candidate)) * weight
    
    result
  }
  
}
