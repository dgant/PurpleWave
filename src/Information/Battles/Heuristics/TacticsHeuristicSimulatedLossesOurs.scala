package Information.Battles.Heuristics
import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMathMultiplicative

object TacticsHeuristicSimulatedLossesOurs extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val simulation = context.simulation(candidate)
    
    if (simulation.isEmpty) return HeuristicMathMultiplicative.default
  
    simulation.get.us.lostValue
  }
}
