package Mathematics.Heuristics

object HeuristicMathAdditive extends HeuristicMath {
  
  val heuristicMaximum  = Double.MaxValue / 10000.0
  val heuristicMinimum  = -heuristicMaximum
  val default           = 0.0
  
  
  
  def order[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidate     : TCandidate)
      : Double = {
    - heuristics.map(_.weighAdditively(context, candidate)).sum
  }
}
