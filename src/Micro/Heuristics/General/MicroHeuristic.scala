package Micro.Heuristics.General

import Micro.Intent.Intention

abstract class MicroHeuristic[T] {
  
  def evaluate(intent:Intention, candidate:T):Double
  
}
