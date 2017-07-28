package Micro.Heuristics.Targeting
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamageAgainst extends TargetHeuristic {
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    state.unit.damageOnNextHitAgainst(candidate)
  }
  
}
