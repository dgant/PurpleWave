package Planning

import Lifecycle.With

object Yolo {
  
  def enabled: Boolean =
    (With.self.supplyUsed > 420 - With.self.minerals / 50) ||
    (With.frame > 24 * 60 * 4
      && With.battles.global.estimation.costToEnemy / 5 > With.battles.global.estimation.costToUs) ||
    ! With.units.ours.exists(_.unitClass.isWorker) ||
    ! With.geography.ourBases.exists(_.mineralsLeft > 0)
  
  def disabled: Boolean = ! enabled
  
}
