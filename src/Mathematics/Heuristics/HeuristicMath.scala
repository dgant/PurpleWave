package Mathematics.Heuristics

abstract class HeuristicMath {
  
  val heuristicMaximum:Double
  val heuristicMinimum:Double
  val default:Double
  
  def clamp(value:Double):Double = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def best[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]] (
    context     : TContext,
    heuristics  : Iterable[THeuristicWeight],
    candidates  : Iterable[TCandidate])
      :TCandidate = {
    candidates.minBy(candidate => order(context, heuristics, candidate))
  }
  
  def sort[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]] (
    context     : TContext,
    heuristics  : Iterable[THeuristicWeight],
    candidates  : Iterable[TCandidate])
      :Vector[TCandidate] = {
    
    candidates.toVector.sortBy(candidate => order(context, heuristics, candidate))
  }
  
  def order[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]] (
    context     : TContext,
    heuristics  : Iterable[THeuristicWeight],
    candidate   : TCandidate)
      : Double
}
