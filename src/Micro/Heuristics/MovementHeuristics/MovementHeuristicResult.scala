package Micro.Heuristics.MovementHeuristics

import Micro.Heuristics.General.{MicroHeuristic, MicroHeuristicResult}
import Micro.Intent.Intention
import bwapi.TilePosition

class MovementHeuristicResult (
  heuristic   : MicroHeuristic[TilePosition],
  intent      : Intention,
  candidate   : TilePosition,
  evaluation  : Double,
  val color   : bwapi.Color)

  extends MicroHeuristicResult (
    heuristic,
    intent,
    candidate,
    evaluation)