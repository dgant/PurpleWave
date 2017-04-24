package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicSimulatedSurvivorsEnemy extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val simulation = context.simulation(candidate)
    
    if (simulation.isEmpty) return HeuristicMath.default
  
    simulation.get.enemy.units.filter(_.alive).map(_.unit.subjectiveValue).sum - simulation.get.enemy.lostValue
  }
}
