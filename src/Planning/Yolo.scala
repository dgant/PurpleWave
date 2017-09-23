package Planning

import Lifecycle.With

object Yolo {
  
  def active: Boolean = (
    (With.self.supplyUsed > 410 - Math.max(2000, With.self.minerals) / 40)
    || ! With.units.ours.exists(_.unitClass.isWorker)
    || With.geography.ourBases.forall(_.mineralsLeft == 0)
    || With.blackboard.allIn
  )
}
