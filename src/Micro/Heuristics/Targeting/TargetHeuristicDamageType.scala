package Micro.Heuristics.Targeting
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamageType extends TargetHeuristic {
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    if ( ! intent.unit.canAttackThisSecond(candidate)) HeuristicMathMultiplicative.default
    
    4.0 * intent.unit.damageScaleAgainst(candidate)
  }
  
}
