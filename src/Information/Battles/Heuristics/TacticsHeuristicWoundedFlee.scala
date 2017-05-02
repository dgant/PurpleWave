package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Mathematics.Heuristics.HeuristicMathMultiplicative

object TacticsHeuristicWoundedFlee extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(candidate.has(Tactics.Wounded.Flee))
    
  }
}