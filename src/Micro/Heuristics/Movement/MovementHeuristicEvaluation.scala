package Micro.Heuristics.Movement

import Mathematics.Heuristics.{Heuristic, HeuristicEvaluation}
import Mathematics.Points.Pixel
import Micro.Execution.ActionState

class MovementHeuristicEvaluation(
    heuristic   : Heuristic[ActionState, Pixel],
    state       : ActionState,
    candidate   : Pixel,
    evaluation  : Double,
    val color   : bwapi.Color)

  extends HeuristicEvaluation (
    heuristic,
    state,
    candidate,
    evaluation)