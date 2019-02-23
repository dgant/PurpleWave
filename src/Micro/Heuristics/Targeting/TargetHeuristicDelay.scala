package Micro.Heuristics.Targeting

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    if ( ! unit.canMove) {
      return if (unit.inRangeToAttack(candidate)) 1.0 else Double.PositiveInfinity
    }

    val range       = unit.pixelRangeAgainst(candidate)
    val theyCanKite = candidate.canAttack(unit) && candidate.pixelRangeAgainst(unit) > unit.pixelRangeAgainst(candidate) + 16
    val speedUs     = if (theyCanKite) 0 else unit.topSpeed
    val freePixels  = unit.cooldownLeft * speedUs

    def adjust(distance: Double): Double = Math.max(0.0, distance - range - freePixels)
    val distanceUs        = adjust(unit.pixelDistanceEdge(candidate))
    val distanceTeamUs    = adjust(candidate.battle.map(_.teamOf(unit)     .centroid.pixelDistance(candidate.pixelCenter)).getOrElse(0.0))
    val distanceTeamEnemy = adjust(candidate.battle.map(_.teamOf(candidate).centroid.pixelDistance(candidate.pixelCenter)).getOrElse(0.0))
    val distanceGoal      = adjust(candidate.pixelDistanceCenter(unit.agent.destination))
    val distanceTotal = (
      distanceUs
      + distanceTeamUs / 3.0
      + distanceTeamEnemy / 5.0
      + distanceGoal / 10.0)
    val framesTotal = distanceTotal / Math.max(1.0, speedUs)
    val frameBuffer = PurpleMath.clamp(unit.matchups.framesOfSafety, 24, 24 * 4)
    val output = frameBuffer + framesTotal
    output
  }
}
