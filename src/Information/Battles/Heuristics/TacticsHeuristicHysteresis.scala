package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicHysteresis extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    HeuristicMath.fromBoolean(context.lastBestTactics == candidate)
  }
}