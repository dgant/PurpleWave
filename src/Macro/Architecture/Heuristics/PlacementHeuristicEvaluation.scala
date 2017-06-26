package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.{Heuristic, HeuristicEvaluation}
import Mathematics.Points.Tile

class PlacementHeuristicEvaluation(
    heuristic   : Heuristic[BuildingDescriptor, Tile],
    state       : BuildingDescriptor,
    candidate   : Tile,
    evaluation  : Double,
    val color   : bwapi.Color)

  extends HeuristicEvaluation (
    heuristic,
    state,
    candidate,
    evaluation)