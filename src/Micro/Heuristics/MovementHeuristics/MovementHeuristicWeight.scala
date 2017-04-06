package Micro.Heuristics.MovementHeuristics

import Debugging.Visualization.Colors
import Mathematics.Heuristics.HeuristicWeight
import bwapi.Color

class MovementHeuristicWeight (
  heuristic : MovementHeuristic,
  weight    : Double,
  val color : Color = Colors.DefaultGray)

    extends HeuristicWeight(heuristic, weight)