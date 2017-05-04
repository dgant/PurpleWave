package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions

object EvaluateTactics {
  
  def best(battle:Battle):TacticsOptions = {
    battle.us.tacticsAvailable.minBy(tactics => evaluate(battle, tactics))
  }
  
  def sort(battle:Battle):Vector[TacticsOptions] = {
    battle.us.tacticsAvailable.sortBy(tactics => evaluate(battle, tactics))
  }
  
  def evaluate(battle:Battle, tactics: TacticsOptions):Double = {
    battle.estimation(tactics).map(estimation => estimation.costToUs - estimation.costToEnemy).sum
  }
}
