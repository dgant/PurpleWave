package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions

object EvaluateTactics {
  
  private val weightSimulation = 1.0
  private val weightEstimation = 1.0
  
  def best(battle:Battle):TacticsOptions = {
    battle.us.tacticsAvailable.minBy(tactics => evaluate(battle, tactics))
  }
  
  def sort(battle:Battle):Vector[TacticsOptions] = {
    battle.us.tacticsAvailable.sortBy(tactics => evaluate(battle, tactics))
  }
  
  def evaluate(battle:Battle, tactics: TacticsOptions):Double = {
    
    val damageToUs =
      weightSimulation * battle.simulation(tactics).map(_.us.lostValue).sum +
      weightEstimation * battle.estimation(tactics).map(_.damageToUs).sum
    
    val damageToEnemy =
      weightSimulation * battle.simulation(tactics).map(_.enemy.lostValue).sum +
      weightEstimation * battle.estimation(tactics).map(_.damageToEnemy).sum
    
    damageToUs - damageToEnemy
  }
}
