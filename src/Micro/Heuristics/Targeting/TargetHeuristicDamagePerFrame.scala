package Micro.Heuristics.Targeting
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamagePerSecond extends TargetHeuristic{
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    candidate.dpfAgainst(state.unit) + Math.max(candidate.groundDpf, candidate.airDpf)
    
  }
}
