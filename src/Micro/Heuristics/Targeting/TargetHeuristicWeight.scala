package Micro.Heuristics.Targeting

import Debugging.Visualizations.Colors
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import Micro.State.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

class TargetHeuristicWeight(
  heuristic : Heuristic[ExecutionState, UnitInfo],
  weight    : Double,
  val color : Color = Colors.DefaultGray)

  extends HeuristicWeight(heuristic, weight)