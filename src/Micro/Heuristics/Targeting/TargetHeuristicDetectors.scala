package Micro.Heuristics.Targeting

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDetectors extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    // Don't prefer detectors when we're engaged
    if (unit.matchups.framesOfSafety <= With.reaction.agencyMax) return HeuristicMathMultiplicative.default
    
    val cloakedFighter = unit.matchups.alliesInclSelfCloaked.find(_.matchups.targets.nonEmpty)
    
    if (cloakedFighter.isEmpty) return HeuristicMathMultiplicative.default
    
    var detects = candidate.unitClass.isDetector
    
    detects ||= candidate.constructing && candidate.target.exists(_.isAny(
      Terran.EngineeringBay,
      Terran.MissileTurret,
      Terran.Academy))
    
    detects ||= candidate.isAny(
      Terran.Comsat,
      Terran.EngineeringBay,
      Terran.ControlTower,
      Protoss.Forge,
      Protoss.Observatory,
      Protoss.RoboticsFacility)
    
    HeuristicMathMultiplicative.fromBoolean(detects)
  }
}
