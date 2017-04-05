package Micro.Heuristics.MovementHeuristics

import Debugging.Visualization.Colors
import Micro.Heuristics.General.MicroHeuristicWeight
import bwapi.Color

class MovementHeuristicWeight (
  heuristic : MovementHeuristic,
  weight    : Double,
  val color : Color = Colors.DefaultGray)

    extends MicroHeuristicWeight(heuristic, weight)