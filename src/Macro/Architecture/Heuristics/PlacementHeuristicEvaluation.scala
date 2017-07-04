package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.{Heuristic, HeuristicEvaluation}
import Mathematics.Points.Tile

class PlacementHeuristicEvaluation(
  heuristic   : Heuristic[Blueprint, Tile],
  state       : Blueprint,
  candidate   : Tile,
  evaluation  : Double,
  val color   : bwapi.Color)

    extends HeuristicEvaluation (
    heuristic,
    state,
    candidate,
    evaluation)