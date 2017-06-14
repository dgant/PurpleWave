package Micro.Heuristics.Movement

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Mathematics.Points.Pixel
import Micro.Task.ExecutionState

class MovementHeuristicResult (
    heuristic   : Heuristic[ExecutionState, Pixel],
    state       : ExecutionState,
    candidate   : Pixel,
    evaluation  : Double,
    val color   : bwapi.Color)

  extends HeuristicResult (
    heuristic,
    state,
    candidate,
    evaluation)