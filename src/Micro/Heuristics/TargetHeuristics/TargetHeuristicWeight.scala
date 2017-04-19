package Micro.Heuristics.TargetHeuristics

import Debugging.Visualizations.Colors
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

class TargetHeuristicWeight(
  heuristic : Heuristic[Intention, UnitInfo],
  weight    : Double,
  val color : Color = Colors.DefaultGray)

  extends HeuristicWeight(heuristic, weight)