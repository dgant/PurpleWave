package Information.Battles

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With

object EvaluateTactics {
  
  def best(battle:Battle):TacticsOptions = {
    if (battle.estimations.isEmpty) return battle.lastBestTactics
    battle.estimations.map(_.tacticsUs).minBy(tactics => evaluate(battle, tactics))
  }
  
  def sort(battle:Battle):Vector[TacticsOptions] = {
    battle.estimations.map(_.tacticsUs).sortBy(tactics => evaluate(battle, tactics))
  }
  
  def evaluate(battle:Battle, tactics: TacticsOptions):Double = {
    battle.estimation(tactics).map(estimation => estimation.costToUs - estimation.costToEnemy * desireToFight(battle)).sum
  }
  
  def desireToFight(battle:Battle):Double = {
    var output = 1.0
    val zone = battle.focus.zone
    if (zone.owner == With.self && zone.bases.nonEmpty) {
      output *= 4.0 / Math.max(1.0, With.geography.ourBases.size)
    }
    output
  }
}
