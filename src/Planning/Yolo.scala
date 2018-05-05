package Planning

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchWorkers

object Yolo {
  
  def active: Boolean = With.blackboard.yoloEnabled && (
    (With.self.supplyUsed > 410 - Math.max(2000, With.self.minerals) / 40)
    || ! With.units.existsOurs(UnitMatchWorkers)
    || With.geography.ourBases.forall(_.mineralsLeft == 0)
    || With.blackboard.allIn
  )
}
