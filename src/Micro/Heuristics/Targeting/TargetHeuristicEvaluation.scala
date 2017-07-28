package Micro.Heuristics.Targeting

import Mathematics.Heuristics.{Heuristic, HeuristicEvaluation}
import Micro.Agency.Intention
import ProxyBwapi.UnitInfo.UnitInfo

class TargetHeuristicEvaluation(
                             heuristic   : Heuristic[Intention, UnitInfo],
                             intent      : Intention,
                             candidate   : UnitInfo,
                             evaluation  : Double,
                             val color   : bwapi.Color)
  
  extends HeuristicEvaluation (
      heuristic,
      intent,
      candidate,
      evaluation)