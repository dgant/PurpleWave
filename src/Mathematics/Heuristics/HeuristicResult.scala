package Mathematics.Heuristics

class HeuristicResult[TContext, TCandidate] (
  val heuristic   : Heuristic[TContext, TCandidate],
  val context     : TContext,
  val candidate   : TCandidate,
  val evaluation  : Double)
