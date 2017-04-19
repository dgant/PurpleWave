package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Mathematics.Pixels.Tile
import Micro.Intent.Intention

class MovementHeuristicResult (
  heuristic   : Heuristic[Intention, Tile],
  intent      : Intention,
  candidate   : Tile,
  evaluation  : Double,
  val color   : bwapi.Color)

  extends HeuristicResult (
    heuristic,
    intent,
    candidate,
    evaluation)