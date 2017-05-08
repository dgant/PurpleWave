package Micro.Heuristics.Targeting
import Micro.State.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamagePerSecond extends TargetHeuristic{
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    candidate.dpsAgainst(state.unit) + Math.max(candidate.groundDps, candidate.airDps)
    
  }
}
