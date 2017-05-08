package Planning

import Lifecycle.With

object Yolo {
  
  def active: Boolean =
    With.configuration.enableYolo && (
      (With.self.supplyUsed > 420 - With.self.minerals / 50)
      || ! With.units.ours.exists(_.unitClass.isWorker)
      || With.geography.ourBases.forall(_.mineralsLeft == 0)
      )
}
