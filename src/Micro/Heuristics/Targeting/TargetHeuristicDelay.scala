package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    if ( ! unit.canMove) {
      return if (unit.inRangeToAttack(candidate)) 1.0 else Double.PositiveInfinity
    }
  
    val range = unit.pixelRangeAgainst(candidate)
    def adjust(distance: Double): Double = Math.max(0.0, distance - range)
    
    val distanceUs        = adjust(unit.pixelDistanceEdge(candidate) - range)
    val distanceTeamUs    = adjust(candidate.battle.map(_.teamOf(unit)     .centroid.pixelDistanceFast(candidate.pixelCenter)).getOrElse(0.0))
    val distanceTeamEnemy = adjust(candidate.battle.map(_.teamOf(candidate).centroid.pixelDistanceFast(candidate.pixelCenter)).getOrElse(0.0))
    val distanceGoal      = adjust(candidate.pixelDistanceCenter(unit.agent.destination))
    val distanceTotal     = distanceUs + distanceTeamUs / 10.0 + distanceTeamEnemy / 10.0 + distanceGoal / 100.0
    distanceTotal
  }
}
