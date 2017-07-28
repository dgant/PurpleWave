package Micro.Heuristics.Targeting

import Debugging.Visualizations.Colors
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Color

class TargetHeuristicWeight(
  heuristic : Heuristic[FriendlyUnitInfo, UnitInfo],
  weight    : Double,
  val color : Color = Colors.DefaultGray)

  extends HeuristicWeight(heuristic, weight)