package Information.Battles.Estimation

import Information.Battles.Types.Battle
import Lifecycle.With

object BattleEstimator {
  
  def run() {
    With.battles.local.foreach(recalculate)
    With.battles.byZone.values.foreach(recalculate)
    recalculate(With.battles.global)
  }
  
  def recalculate(battle: Battle) {
    battle.estimationGeometric = new BattleEstimation(Some(battle), considerGeometry = true)
    battle.estimationGeometric.addUnits(battle)
    battle.estimationGeometric.recalculate()
    battle.estimationAbstract = new BattleEstimation(Some(battle), considerGeometry = false)
    battle.estimationAbstract.addUnits(battle)
    battle.estimationAbstract.recalculate()
  }
}
