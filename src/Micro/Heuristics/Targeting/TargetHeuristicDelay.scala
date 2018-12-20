package Micro.Heuristics.Targeting

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    if ( ! unit.canMove) {
      return if (unit.inRangeToAttack(candidate)) 1.0 else Double.PositiveInfinity
    }


    val range = unit.pixelRangeAgainst(candidate)
    val freePixels = unit.cooldownLeft * unit.topSpeed
    def adjust(distance: Double): Double = Math.max(0.0, distance - range - freePixels)

    val candidateSpeed    = if (candidate.gathering) 0 else candidate.topSpeed
    val theyCanKite       = candidate.canAttack(unit) && candidate.pixelRangeAgainst(unit) > unit.pixelRangeAgainst(candidate) + 16
    val distanceBonus     = if (theyCanKite) Math.max(0, candidateSpeed - unit.topSpeed) * Math.max(0, unit.pixelDistanceEdge(candidate) - unit.pixelRangeAgainst(candidate)) else 0
    val distanceUs        = adjust(unit.pixelDistanceEdge(candidate) - range)
    val distanceTeamUs    = adjust(candidate.battle.map(_.teamOf(unit)     .centroid.pixelDistance(candidate.pixelCenter)).getOrElse(0.0))
    val distanceTeamEnemy = adjust(candidate.battle.map(_.teamOf(candidate).centroid.pixelDistance(candidate.pixelCenter)).getOrElse(0.0))
    val distanceGoal      = adjust(candidate.pixelDistanceCenter(unit.agent.destination))
    val distanceTotal     = distanceUs + distanceTeamUs / 10.0 + distanceTeamEnemy / 10.0 + distanceGoal / 100.0
    distanceTotal
  }
}
