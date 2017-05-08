package Micro.Heuristics.Targeting
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.State.ExecutionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamageType extends TargetHeuristic {
  
  override def evaluate(state: ExecutionState, candidate: UnitInfo): Double = {
    
    if ( ! state.unit.canAttackThisSecond(candidate)) HeuristicMathMultiplicative.default
    
    4.0 * state.unit.damageScaleAgainst(candidate)
  }
  
}
