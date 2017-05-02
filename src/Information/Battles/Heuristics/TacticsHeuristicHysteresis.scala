package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMathMultiplicative

object TacticsHeuristicHysteresis extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    HeuristicMathMultiplicative.fromBoolean(context.lastBestTactics == candidate)
  }
}