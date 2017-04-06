package Micro.Heuristics.MovementHeuristics

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Micro.Intent.Intention
import bwapi.TilePosition

class MovementHeuristicResult (
                                heuristic   : Heuristic[Intention, TilePosition],
                                intent      : Intention,
                                candidate   : TilePosition,
                                evaluation  : Double,
                                val color   : bwapi.Color)

  extends HeuristicResult (
    heuristic,
    intent,
    candidate,
    evaluation)