package Information.Battles.Heuristics
import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicSimulatedLosses extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val simulation = context.simulation(candidate)
    
    if (simulation.isEmpty) return HeuristicMath.default
  
    Math.max(1.0, simulation.get.enemy.lostValue) / Math.max(1.0, simulation.get.us.lostValue)
  }
}
