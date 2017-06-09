package Information.Battles.Estimation

import Lifecycle.With

object BattleEstimator {
  
  def run() {
    recalculateLocal()
    recalculateZones()
    recalculateGlobal()
  }
  
  def recalculateLocal() {
    With.battles.local.foreach(battle => {
      battle.estimation = new BattleEstimation(Some(battle), considerGeometry = true)
      battle.estimation.addUnits(battle)
      battle.estimation.recalculate()
    })
  }
  
  def recalculateZones() {
    With.battles.byZone.values.foreach(battle => {
      battle.estimation = new BattleEstimation(Some(battle), considerGeometry = false)
      battle.estimation.addUnits(battle)
      battle.estimation.recalculate()
    })
  }
  
  def recalculateGlobal() {
    val battle = With.battles.global
    battle.estimation = new BattleEstimation(Some(battle), considerGeometry = false)
    battle.estimation.addUnits(battle)
    battle.estimation.recalculate()
  }
}
