package Micro.Heuristics.Movement

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

class MovementHeuristicResult (
    heuristic   : Heuristic[ActionState, Pixel],
    state       : ActionState,
    candidate   : Pixel,
    evaluation  : Double,
    val color   : bwapi.Color)

  extends HeuristicResult (
    heuristic,
    state,
    candidate,
    evaluation)