package Mathematics.Heuristics

class HeuristicEvaluation[TContext, TCandidate](
  val heuristic   : Heuristic[TContext, TCandidate],
  val context     : TContext,
  val candidate   : TCandidate,
  val evaluation  : Double)
