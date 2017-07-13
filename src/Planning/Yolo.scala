package Planning

import Lifecycle.With

object Yolo {
  
  def active: Boolean =
    With.configuration.enableYolo && (
      (With.self.supplyUsed > 410 - With.self.minerals / 20)
      || ! With.units.ours.exists(_.unitClass.isWorker)
      || With.geography.ourBases.forall(_.mineralsLeft == 0))
}
