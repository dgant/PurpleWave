package Micro.Heuristics.Targeting

import Mathematics.Heuristics.{Heuristic, HeuristicResult}
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

class TargetHeuristicResult(
                             heuristic   : Heuristic[Intention, UnitInfo],
                             intent      : Intention,
                             candidate   : UnitInfo,
                             evaluation  : Double,
                             val color   : bwapi.Color)
  
  extends HeuristicResult (
      heuristic,
      intent,
      candidate,
      evaluation)