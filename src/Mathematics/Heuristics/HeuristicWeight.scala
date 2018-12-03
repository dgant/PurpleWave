package Mathematics.Heuristics

class HeuristicWeight[TContext, TCandidate] (
  val heuristic : Heuristic[TContext, TCandidate],
  val weight    : Double) {
  
  def apply(context: TContext, candidate: TCandidate): Double = {
    val result =
      if (weight == 0.0) 1.0 else Math.pow(
        HeuristicMathMultiplicative.clamp(heuristic.evaluate(context, candidate)),
        weight)
    
    result
  }
}
