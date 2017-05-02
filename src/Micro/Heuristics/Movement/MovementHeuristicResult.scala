package Micro.Heuristics.Movement

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention

class MovementHeuristicResult (
  heuristic   : Heuristic[Intention, Pixel],
  intent      : Intention,
  candidate   : Pixel,
  evaluation  : Double,
  val color   : bwapi.Color)

  extends HeuristicResult (
    heuristic,
    intent,
    candidate,
    evaluation)