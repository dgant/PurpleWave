package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicEstimatedStrength extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val estimation = context.estimation(candidate)
    
    if (estimation.isEmpty) return HeuristicMath.default
  
    //TODO: Clearly, not this
    1.0
    //Math.max(1.0, estimation.get.enemy.lostValue) / Math.max(1.0, estimation.get.us.lostValue)
  }
}
