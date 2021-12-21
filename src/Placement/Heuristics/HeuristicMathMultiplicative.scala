package Placement.Heuristics

object HeuristicMathMultiplicative {
  
  val heuristicMaximum  = 100000.0
  val heuristicMinimum  = 1.0
  val default           = heuristicMinimum
  
  def fromBoolean(value: Boolean): Double = if (value) 2.0 else 1.0

  def clamp(value: Double): Double = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def resolve[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidate     : TCandidate)
      : Double = {

    var output = -1.0
    heuristics.foreach(output *= _.apply(context, candidate))
    output
  }
}
