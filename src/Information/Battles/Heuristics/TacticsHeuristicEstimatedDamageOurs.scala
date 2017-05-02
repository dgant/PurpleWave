package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMathMultiplicative

object TacticsHeuristicEstimatedDamageOurs extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val estimation = context.estimation(candidate)
    
    if (estimation.isEmpty) return HeuristicMathMultiplicative.default
  
    estimation.get.damageToUs
  }
}
