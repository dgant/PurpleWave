package Micro.Heuristics.TargetHeuristics

import Debugging.Visualization.Colors
import Mathematics.Heuristics.HeuristicWeight
import bwapi.Color

class TargetHeuristicWeight(
  heuristic : TargetHeuristic,
  weight    : Double,
  val color : Color = Colors.DefaultGray)

  extends HeuristicWeight(heuristic, weight) {
  
}
