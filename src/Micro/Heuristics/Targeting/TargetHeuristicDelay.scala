package Micro.Heuristics.Targeting

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDelay extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    if ( ! unit.canMove) {
      return if (unit.inRangeToAttack(candidate)) 1.0 else Double.PositiveInfinity
    }
    
    val distanceUs          = unit.pixelDistanceEdge(candidate) - unit.pixelRangeAgainst(candidate)
    val distanceTeam        = candidate.battle.map(_.teamOf(candidate).centroid.pixelDistanceFast(candidate.pixelCenter)).getOrElse(0.0)
    val distanceGoal        = candidate.pixelDistanceCenter(unit.agent.destination)
    val distanceTotal       = distanceUs + distanceTeam
    val closingSpeed        = Math.max(unit.topSpeed / 4.0, unit.topSpeed - candidate.topSpeed / 2.0)
    val radiansAway         = Math.abs(unit.angleRadians - unit.pixelCenter.radiansTo(candidate.pixelCenter))
    val framesToClose       = PurpleMath.nanToInfinity(distanceTotal / closingSpeed)
    val framesToTurn        = unit.unitClass.framesToTurn(radiansAway)
    val framesFreedomMoving = unit.matchups.pixelsOfFreedom / unit.topSpeed
    val framesFreedomBase   = PurpleMath.clamp(unit.matchups.framesOfSafetyDiffused, 8.0, 48.0)
    val framesFreedomLife   = unit.matchups.framesToLiveDiffused
    val framesFreedom       = Iterable(framesFreedomMoving, framesFreedomBase, framesFreedomLife).min
    val output              = framesFreedom + unit.cooldownLeft + framesToClose + framesToTurn
    output
  }
}
