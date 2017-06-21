package Micro.Heuristics.Targeting

import Debugging.Visualizations.Colors
import Mathematics.Heuristics.{Heuristic, HeuristicWeight}
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

class TargetHeuristicWeight(
                             heuristic : Heuristic[ActionState, UnitInfo],
                             weight    : Double,
                             val color : Color = Colors.DefaultGray)

  extends HeuristicWeight(heuristic, weight)