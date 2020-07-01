package Mathematics.Heuristics

abstract class HeuristicMath {
  
  val heuristicMaximum: Double
  val heuristicMinimum: Double
  val default: Double
  
  def clamp(value: Double): Double = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def resolve[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context     : TContext,
    heuristics  : Iterable[THeuristicWeight],
    candidate   : TCandidate)
      : Double
}
