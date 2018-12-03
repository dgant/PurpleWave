package Mathematics.Heuristics

object HeuristicMathMultiplicative extends HeuristicMath {
  
  val heuristicMaximum  = 100000.0
  val heuristicMinimum  = 1.0
  val default           = heuristicMinimum
  
  def fromBoolean(value:Boolean):Double = if (value) 2.0 else 1.0
  
  def resolve[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidate     : TCandidate)
      : Double = {

    var output = -1.0
    for (h <- heuristics) {
      output *= h.apply(context, candidate)
    }
    output
  }
}
