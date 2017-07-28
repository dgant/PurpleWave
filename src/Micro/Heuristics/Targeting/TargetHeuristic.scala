package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

abstract class TargetHeuristic extends Heuristic[FriendlyUnitInfo, UnitInfo] {
  
  def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double
  
}
