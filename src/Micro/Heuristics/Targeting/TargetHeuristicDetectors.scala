package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDetectors extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    // Don't reach for detectors when we're engaged
    if (unit.matchups.framesOfSafety <= 0 && ! unit.inRangeToAttack(candidate)) return HeuristicMathMultiplicative.default
    
    lazy val cloakedFighter = unit.matchups.alliesInclSelfCloaked.find(_.matchups.targets.nonEmpty)
    if (unit.matchups.nearestArbiter.isEmpty && cloakedFighter.isEmpty) return HeuristicMathMultiplicative.default
    
    var detects = candidate.unitClass.isDetector
    
    detects ||= (candidate.constructing || candidate.repairing) && candidate.target.exists(_.isAny(
      Terran.EngineeringBay,
      Terran.MissileTurret,
      Terran.Academy))
    
    detects ||= candidate.isAny(
      Terran.Comsat,
      Terran.ControlTower,
      Protoss.RoboticsFacility)

    detects ||= ! candidate.complete && candidate.isAny(
      Terran.EngineeringBay,
      Protoss.Forge,
      Protoss.Observatory
    )

    // Clear the way!
    detects ||= (
      unit.zone != unit.agent.destination.zone
      && unit.agent.destination.zone.exitNow.exists(_.pixelCenter.pixelDistance(candidate.pixelCenter) < 32 * 5))
    
    HeuristicMathMultiplicative.fromBoolean(detects)
  }
}
