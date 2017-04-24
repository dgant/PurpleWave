package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.Heuristics._
import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath

object EvaluateTactics {
  
  private val heuristicWeights = Vector(
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedSurvivorsOurs,   1.0),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedLossesEnemy,     1.0),
    new TacticsHeuristicWeight(TacticsHeuristicEstimatedDamageEnemy,     1.0),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedSurvivorsEnemy, -1.0),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedLossesOurs,     -1.0),
    new TacticsHeuristicWeight(TacticsHeuristicEstimatedDamageOurs,     -1.0),
    new TacticsHeuristicWeight(TacticsHeuristicFleeingWhenDefending,    -1.0)
  )
  
  def best(battle:Battle):TacticsOptions = {
    val candidates = battle.us.tacticsAvailable
  
    HeuristicMath.best(battle, heuristicWeights, candidates)
  }
  
  def sort(battle:Battle):Vector[TacticsOptions] = {
    
    val candidates = battle.us.tacticsAvailable
    
    if (With.configuration.visualize && With.configuration.visualizeBattles) {
      battle.tacticsHeuristicResults =
        candidates.flatten(candidate =>
          heuristicWeights.map(heuristicWeight =>
            new TacticsHeuristicResult(
              heuristicWeight.heuristic,
              battle,
              candidate,
              heuristicWeight.weigh(battle, candidate))))
    }
  
    HeuristicMath.sort(battle, heuristicWeights, candidates)
  }
}
