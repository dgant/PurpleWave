package Micro.Heuristics.TargetHeuristics

import Debugging.Visualization.Colors
import Micro.Heuristics.General.MicroHeuristicWeight
import bwapi.Color

class TargetHeuristicWeight(
  heuristic : TargetHeuristic,
  weight    : Double,
  val color : Color = Colors.DefaultGray)

    extends MicroHeuristicWeight(heuristic, weight) {
  
}
