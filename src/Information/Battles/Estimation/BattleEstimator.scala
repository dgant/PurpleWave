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
      battle.estimationGeometric = new BattleEstimation(Some(battle), considerGeometry = true)
      battle.estimationGeometric.addUnits(battle)
      battle.estimationGeometric.recalculate()
    })
  }
  
  def recalculateZones() {
    With.battles.byZone.values.foreach(battle => {
      battle.estimationGeometric = new BattleEstimation(Some(battle), considerGeometry = false)
      battle.estimationGeometric.addUnits(battle)
      battle.estimationGeometric.recalculate()
    })
  }
  
  def recalculateGlobal() {
    val battle = With.battles.global
    battle.estimationGeometric = new BattleEstimation(Some(battle), considerGeometry = false)
    battle.estimationGeometric.addUnits(battle)
    battle.estimationGeometric.recalculate()
  }
}
