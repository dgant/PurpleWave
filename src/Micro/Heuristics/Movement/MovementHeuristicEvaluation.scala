package Micro.Heuristics.Movement

import Mathematics.Heuristics.{Heuristic, HeuristicEvaluation}
import Mathematics.Points.Pixel
import Micro.Agency.Agent

class MovementHeuristicEvaluation(
                                   heuristic   : Heuristic[Agent, Pixel],
                                   state       : Agent,
                                   candidate   : Pixel,
                                   evaluation  : Double,
                                   val color   : bwapi.Color)

  extends HeuristicEvaluation (
    heuristic,
    state,
    candidate,
    evaluation)