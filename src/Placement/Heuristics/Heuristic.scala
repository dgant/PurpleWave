package Placement.Heuristics

abstract class Heuristic[TContext, TCandidate] {
  
  def evaluate(context:TContext, candidate:TCandidate):Double
  
}
