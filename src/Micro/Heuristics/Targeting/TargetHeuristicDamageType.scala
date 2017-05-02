package Micro.Heuristics.Targeting
import Mathematics.Heuristics.HeuristicMath
import Micro.Intent.Intention
import ProxyBwapi.UnitInfo.UnitInfo

object TargetHeuristicDamageType extends TargetHeuristic {
  
  override def evaluate(intent: Intention, candidate: UnitInfo): Double = {
    
    if ( ! intent.unit.canAttackThisSecond(candidate)) HeuristicMath.default
    
    4.0 * intent.unit.damageScaleAgainst(candidate)
  }
  
}
