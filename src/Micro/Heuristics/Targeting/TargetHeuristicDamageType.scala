package Micro.Heuristics.Targeting
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Execution.ActionState
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamageType extends TargetHeuristic {
  
  override def evaluate(state: ActionState, candidate: UnitInfo): Double = {
    
    if ( ! state.unit.canAttack(candidate)) HeuristicMathMultiplicative.default
    
    4.0 * state.unit.damageScaleAgainst(candidate)
  }
  
}
