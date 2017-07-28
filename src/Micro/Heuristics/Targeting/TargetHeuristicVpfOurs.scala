package Micro.Heuristics.Targeting
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfOurs extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    MicroValue.valuePerAttack(unit, candidate) / unit.cooldownMaxAgainst(candidate)
  }
  
}
