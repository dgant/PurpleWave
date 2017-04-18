package Planning

import Lifecycle.With

object Yolo {
  
  def enabled: Boolean =
    (With.self.supplyUsed > 400 - With.self.minerals / 500) ||
    (With.frame > 24 * 60 * 4 && With.battles.global.enemy.strength > 0 && With.battles.global.us.strength / 10 > With.battles.global.enemy.strength) ||
    ! With.units.ours.exists(_.unitClass.isWorker) ||
    ! With.geography.ourBases.exists(_.mineralsLeft > 0)
  
  def disabled: Boolean = ! enabled
  
}
