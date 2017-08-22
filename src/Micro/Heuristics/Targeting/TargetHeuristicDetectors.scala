package Micro.Heuristics.Targeting

import Mathematics.Heuristics.HeuristicMathMultiplicative
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicDetectors extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    
    val cloakedFighter = unit.matchups.alliesIncludingSelf.find(ally => ally.cloaked && ally.matchups.targets.nonEmpty)
    
    if (cloakedFighter.isEmpty) return HeuristicMathMultiplicative.default
    
    var detects = candidate.unitClass.isDetector
    
    detects ||= candidate.constructing && candidate.target.exists(_.is(
      Terran.EngineeringBay,
      Terran.MissileTurret,
      Terran.Academy))
    
    detects ||= candidate.is(
      Terran.Comsat,
      Terran.EngineeringBay,
      Terran.ControlTower,
      Protoss.Forge,
      Protoss.Observatory,
      Protoss.RoboticsFacility)
    
    HeuristicMathMultiplicative.fromBoolean(detects)
  }
}
