package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions

object EvaluateTactics {
  
  def best(battle:Battle):TacticsOptions = {
    battle.estimations.map(_.tacticsUs).minBy(tactics => evaluate(battle, tactics))
  }
  
  def sort(battle:Battle):Vector[TacticsOptions] = {
    battle.estimations.map(_.tacticsUs).sortBy(tactics => evaluate(battle, tactics))
  }
  
  def evaluate(battle:Battle, tactics: TacticsOptions):Double = {
    battle.estimation(tactics).map(estimation => estimation.costToUs - estimation.costToEnemy).sum
  }
}
