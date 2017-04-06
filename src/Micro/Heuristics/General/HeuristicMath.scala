package Micro.Heuristics.General

import Micro.Intent.Intention

object HeuristicMath {
  
  val heuristicMaximum = 100000.0
  val heuristicMinimum = 1.0
  val default = heuristicMinimum
  
  def fromBoolean(value:Boolean):Double = if (value) 2.0 else 1.0
  def normalize(value:Double) = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def calculateBest[T](
    intent        : Intention,
    heuristics    : Iterable[MicroHeuristicWeight[T]],
    candidates    : Iterable[T]):T = {
    
    candidates.maxBy(candidate =>
      heuristics
        .map(_.weigh(intent, candidate))
        .product)
  }
}
