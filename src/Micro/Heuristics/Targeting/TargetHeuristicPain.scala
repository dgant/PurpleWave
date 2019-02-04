package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicPain extends TargetHeuristic {

  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    unit.enemyRangeGrid.get(candidate.pixelCenter.project(unit.pixelCenter, unit.pixelRangeAgainst(candidate)).tileIncluding)
  }
}
