package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Lifecycle.With

object BattleEstimator {
  
  def run() {
    With.battles.all.foreach(recalculate)
  }
  
  def recalculate(battle:Battle) {
    battle.estimations =
      battle.us.tacticsAvailable.map(tacticsUs => {
        val tacticsEnemy = battle.enemy.tacticsApparent
        val estimation = new BattleEstimation(tacticsUs, tacticsEnemy)
        estimation.addUnits(battle)
        estimation.recalculate()
        estimation
      })
  }
}
