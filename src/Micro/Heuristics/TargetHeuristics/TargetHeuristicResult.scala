package Micro.Heuristics.TargetHeuristics

import Micro.Heuristics.General.{MicroHeuristic, MicroHeuristicResult}
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

class TargetHeuristicResult(
  heuristic   : MicroHeuristic[UnitInfo],
  intent      : Intention,
  candidate   : UnitInfo,
  evaluation  : Double,
  val color   : bwapi.Color)
  
  extends MicroHeuristicResult (
      heuristic,
      intent,
      candidate,
      evaluation)