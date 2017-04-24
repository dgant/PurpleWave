package Information.Battles

import Information.Battles.Heuristics.{TacticsHeuristicEstimatedStrength, TacticsHeuristicSimulatedLosses, TacticsHeuristicWeight}
import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object EvaluateTactics {
  
  private val heuristicWeights = Vector(
    new TacticsHeuristicWeight(TacticsHeuristicEstimatedStrength, 1.0),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedLosses,   -1.0)
  )
  
  def best(battle:Battle):TacticsOptions = {
    
    val candidates = battle.us.tacticsAvailable
  
    HeuristicMath.calculateBest(battle, heuristicWeights, candidates)
  }
}
