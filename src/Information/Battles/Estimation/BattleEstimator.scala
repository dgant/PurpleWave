package Information.Battles.Estimation

import Information.Battles.TacticsTypes.TacticsDefault
import Lifecycle.With

object BattleEstimator {
  
  def run() {
    recalculateLocal()
    recalculateZones()
    recalculateGlobal()
  }
  
  def recalculateLocal() {
    With.battles.local.foreach(battle =>
      battle.estimations =
        battle.us.tacticsAvailable.map(tacticsUs => {
          battle.enemy.tacticsAvailable.map(tacticsEnemy => {
            val estimation = new BattleEstimation(tacticsUs, tacticsEnemy, Some(battle), considerGeometry = true)
            estimation.addUnits(battle)
            estimation.recalculate()
            estimation
          })
          .minBy(estimation => estimation.result.costToEnemy - estimation.result.costToUs)
        }))
  }
  
  def recalculateZones() {
    With.battles.byZone.values.foreach(battle => {
      val estimation = new BattleEstimation(TacticsDefault.get, TacticsDefault.get, Some(battle), considerGeometry = false)
      estimation.addUnits(battle)
      estimation.recalculate()
      battle.estimations = Vector(estimation)
    })
  }
  
  def recalculateGlobal() {
    val battle = With.battles.global
    val estimation = new BattleEstimation(TacticsDefault.get, TacticsDefault.get, Some(battle), considerGeometry = false)
    estimation.addUnits(battle)
    estimation.recalculate()
    battle.estimations = Vector(estimation)
  }
}
