package Micro.Heuristics.General

import Micro.Intent.Intention

class MicroHeuristicResult[T] (
  val heuristic   : MicroHeuristic[T],
  val intent      : Intention,
  val candidate   : T,
  val evaluation  : Double)
