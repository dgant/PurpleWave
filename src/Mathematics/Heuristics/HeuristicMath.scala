package Mathematics.Heuristics


object HeuristicMath {
  
  val heuristicMaximum = 100000.0
  val heuristicMinimum = 1.0
  val default = heuristicMinimum
  
  def fromBoolean(value:Boolean):Double = if (value) 2.0 else 1.0
  def clamp(value:Double):Double = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def best[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidates    : Iterable[TCandidate])
      :TCandidate = {
    
    candidates.minBy(candidate => order(context, heuristics, candidate))
  }
  
  def sort[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidates    : Iterable[TCandidate])
      :Vector[TCandidate] = {
    
    candidates.toVector.sortBy(candidate => order(context, heuristics, candidate))
  }
  
  def order[TContext, TCandidate, THeuristic, THeuristicWeight <: HeuristicWeight[TContext, TCandidate]](
    context       : TContext,
    heuristics    : Iterable[THeuristicWeight],
    candidate     : TCandidate)
      : Double = {
    - heuristics.map(_.weigh(context, candidate)).product
  }
}
