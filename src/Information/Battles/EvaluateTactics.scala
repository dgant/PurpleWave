package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.Heuristics._
import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath

object EvaluateTactics {
  
  private val weightSimulation = 1.0
  private val weightEvaluation = 1.0
  private val heuristicWeights = Vector(
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedSurvivorsOurs,   weightSimulation),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedLossesEnemy,     weightSimulation),
    new TacticsHeuristicWeight(TacticsHeuristicEstimatedDamageEnemy,     weightEvaluation),
    new TacticsHeuristicWeight(TacticsHeuristicHysteresis,               1.25),
    new TacticsHeuristicWeight(TacticsHeuristicKiting,                   0.00),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedSurvivorsEnemy, -weightSimulation),
    new TacticsHeuristicWeight(TacticsHeuristicSimulatedLossesOurs,     -weightSimulation),
    new TacticsHeuristicWeight(TacticsHeuristicEstimatedDamageOurs,     -weightEvaluation),
    new TacticsHeuristicWeight(TacticsHeuristicFleeingWhenDefending,    -0.5),
    new TacticsHeuristicWeight(TacticsHeuristicWorkersSallying,         -1.0)
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
